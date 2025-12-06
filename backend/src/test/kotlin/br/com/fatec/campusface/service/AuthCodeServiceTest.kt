package br.com.fatec.campusface.service

import br.com.fatec.campusface.dto.OrganizationMemberDTO
import br.com.fatec.campusface.dto.UserDTO
import br.com.fatec.campusface.models.AuthCode
import br.com.fatec.campusface.models.MemberStatus
import br.com.fatec.campusface.models.OrganizationMember
import br.com.fatec.campusface.models.Role
import br.com.fatec.campusface.repository.AuthCodeRepository
import br.com.fatec.campusface.repository.OrganizationMemberRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.time.temporal.ChronoUnit

class AuthCodeServiceTest {

    @MockK
    lateinit var authCodeRepository: AuthCodeRepository

    @MockK
    lateinit var orgMemberRepository: OrganizationMemberRepository

    @MockK
    lateinit var orgMemberService: OrganizationMemberService

    @InjectMockKs
    lateinit var authCodeService: AuthCodeService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    // --- CENÁRIOS DE GERAÇÃO ---

    @Test
    fun `generateCode deve criar novo codigo e invalidar anteriores`() {
        val userId = "user123"
        val orgId = "orgABC"
        val activeMember = OrganizationMember(userId = userId, organizationId = orgId, status = MemberStatus.ACTIVE)

        every { orgMemberRepository.findByUserIdAndOrganizationId(userId, orgId) } returns activeMember
        every { authCodeRepository.invalidatePreviousCodes(userId, orgId) } just Runs
        every { authCodeRepository.save(any()) } returnsArgument 0

        val result = authCodeService.generateCode(userId, orgId)

        assertNotNull(result.code)
        verify(exactly = 1) { authCodeRepository.invalidatePreviousCodes(userId, orgId) }
    }

    @Test
    fun `generateCode deve lancar erro se usuario estiver INATIVO ou PENDENTE`() {
        // ARRANGE
        val userId = "userBanido"
        val orgId = "orgABC"

        // SIMULAÇÃO: O membro existe, mas o status é INACTIVE (ou PENDING)
        val inactiveMember = OrganizationMember(
            userId = userId,
            organizationId = orgId,
            status = MemberStatus.INACTIVE
        )

        every { orgMemberRepository.findByUserIdAndOrganizationId(userId, orgId) } returns inactiveMember

        // ACT & ASSERT
        val exception = assertThrows<IllegalStateException> {
            authCodeService.generateCode(userId, orgId)
        }

        assertTrue(exception.message!!.contains("não está ativo"))
        assertTrue(exception.message!!.contains("INACTIVE"))
    }

    @Test
    fun `generateCode deve lancar erro se usuario nao for membro da organizacao`() {
        // ARRANGE
        val userId = "userIntruso"
        val orgId = "orgSecreta"

        // SIMULAÇÃO: o repositório retorna null (não achou o membro)
        every { orgMemberRepository.findByUserIdAndOrganizationId(userId, orgId) } returns null

        // ACT & ASSERT
        val exception = assertThrows<IllegalArgumentException> {
            authCodeService.generateCode(userId, orgId)
        }

        // Verifica se a mensagem de erro é a esperada
        assertEquals("Você não é membro desta organização.", exception.message)
    }


    @Test
    fun `validateCode deve retornar invalido se o codigo nao existir`() {
        every { authCodeRepository.findValidByCode("999999") } returns null

        val result = authCodeService.validateCode("999999", "fiscal")

        assertFalse(result.valid)
        assertEquals("Código inválido, não encontrado ou já utilizado.", result.message)
    }

    @Test
    fun `validateCode deve falhar se codigo estiver expirado`() {
        val expiredCode = AuthCode(
            id = "expiredId",
            expirationTime = Instant.now().minus(1, ChronoUnit.SECONDS),
            valid = true
        )

        every { authCodeRepository.findValidByCode("123456") } returns expiredCode
        every { authCodeRepository.invalidateCode("expiredId") } just Runs

        val result = authCodeService.validateCode("123456", "fiscal")

        assertFalse(result.valid)
        assertEquals("Código expirado.", result.message)
    }

    @Test
    fun `validateCode deve lancar erro se fiscal nao tiver permissao`() {
        val orgId = "orgFatec"
        val validCode = AuthCode(organizationId = orgId, expirationTime = Instant.now().plusSeconds(60))
        val unauthorizedMember = OrganizationMember(role = Role.MEMBER, status = MemberStatus.ACTIVE)

        every { authCodeRepository.findValidByCode("123456") } returns validCode
        every { orgMemberRepository.findByUserIdAndOrganizationId("intruso", orgId) } returns unauthorizedMember

        assertThrows<IllegalAccessException> {
            authCodeService.validateCode("123456", "intruso")
        }
    }

    @Test
    fun `validateCode deve falhar se usuario dono do codigo nao for encontrado`() {
        val orgId = "orgFatec"
        val validCode = AuthCode(userId = "fantasma", organizationId = orgId, expirationTime = Instant.now().plusSeconds(60))
        val fiscal = OrganizationMember(role = Role.VALIDATOR, status = MemberStatus.ACTIVE)

        every { authCodeRepository.findValidByCode("123456") } returns validCode
        every { orgMemberRepository.findByUserIdAndOrganizationId("fiscal", orgId) } returns fiscal
        every { authCodeRepository.invalidateCode(any()) } just Runs
        every { orgMemberRepository.findByUserIdAndOrganizationId("fantasma", orgId) } returns null

        val result = authCodeService.validateCode("123456", "fiscal")

        assertFalse(result.valid)
        assertEquals("Usuário do código não encontrado na organização.", result.message)
    }

    @Test
    fun `validateCode deve retornar Sucesso quando tudo estiver correto`() {
        val orgId = "orgFatec"
        val userId = "userOk"
        val validCode = AuthCode(id="c1", userId = userId, organizationId = orgId, expirationTime = Instant.now().plusSeconds(60))
        val fiscal = OrganizationMember(role = Role.VALIDATOR, status = MemberStatus.ACTIVE)
        val targetMember = OrganizationMember(id = "m1", userId = userId, organizationId = orgId)

        val userDto = UserDTO(id = userId, fullName = "Teste", email = "t@t.com", document = "1", faceImageId = "f", createdAt = Instant.now(), updatedAt = Instant.now())
        val memberDto = OrganizationMemberDTO(id = "m1", role = Role.MEMBER, status = MemberStatus.ACTIVE, joinedAt = Instant.now(), user = userDto)

        every { authCodeRepository.findValidByCode("123456") } returns validCode
        every { orgMemberRepository.findByUserIdAndOrganizationId("fiscal", orgId) } returns fiscal
        every { authCodeRepository.invalidateCode("c1") } just Runs
        every { orgMemberRepository.findByUserIdAndOrganizationId(userId, orgId) } returns targetMember
        every { orgMemberService.getMemberById("m1") } returns memberDto

        val result = authCodeService.validateCode("123456", "fiscal")

        assertTrue(result.valid)
        assertEquals("Acesso Autorizado!", result.message)
        assertNotNull(result.member)
    }

    @Test
    fun `validateCode deve lancar IllegalAccessException quando o validador e possui uma ROLE mas nao está ativo`() {
        // ARRANGE
        val codeStr = "123456"
        val validatorId = "validator-user-id"
        val orgId = "org-id"

        // Mock do QR Code válido
        val validAuthCode = AuthCode(
            code = codeStr,
            userId = "member-id",
            organizationId = orgId,
            expirationTime = Instant.now().plusSeconds(60)
        )

        val inactiveValidator = OrganizationMember(
            id = "member-ref-id",
            userId = validatorId,
            organizationId = orgId,
            role = Role.VALIDATOR,
            status = MemberStatus.INACTIVE
        )

        // Configurando os Mocks
        every { authCodeRepository.findValidByCode(codeStr) } returns validAuthCode
        every { orgMemberRepository.findByUserIdAndOrganizationId(validatorId, orgId) } returns inactiveValidator

        // ACT & ASSERT
        val exception = assertThrows<IllegalAccessException> {
            authCodeService.validateCode(codeStr, validatorId)
        }

        assertEquals("Você não tem permissão de VALIDATOR nesta organização.", exception.message)

        // Verifica se NÃO invalidou o código (pois foi erro de fiscal, não do código)
        verify(exactly = 0) { authCodeRepository.invalidateCode(any()) }
    }


    @Test
    fun `validateCode deve retornar sucesso quando o VALIDADOR possui a ROLE ADMIN`() {
        // ARRANGE
        val codeStr = "123456"
        val validatorId = "admin-id"
        val orgId = "org-id"

        val validAuthCode = AuthCode(code = codeStr, userId = "user-id", organizationId = orgId, expirationTime = Instant.now().plusSeconds(60))
        val targetMember = OrganizationMember(id = "m1", userId = "user-id", organizationId = orgId)

        val adminValidator = OrganizationMember(
            id = "admin-ref", userId = validatorId, organizationId = orgId,
            role = Role.ADMIN, status = MemberStatus.ACTIVE
        )

        every { authCodeRepository.findValidByCode(codeStr) } returns validAuthCode
        every { orgMemberRepository.findByUserIdAndOrganizationId(validatorId, orgId) } returns adminValidator
        every { authCodeRepository.invalidateCode(any()) } just Runs
        every { orgMemberRepository.findByUserIdAndOrganizationId("user-id", orgId) } returns targetMember
        every { orgMemberService.getMemberById(any()) } returns mockk()

        // ACT
        val response = authCodeService.validateCode(codeStr, validatorId)

        // ASSERT
        assertTrue(response.valid)
    }


    @Test
    fun `validateCode deve falhar quando o validador tem a ROLE MEMBER`() {
        // ARRANGE
        val codeStr = "123456"
        val validatorId = "member-id"
        val orgId = "org-id"
        val validAuthCode = AuthCode(code = codeStr, userId = "u", organizationId = orgId, expirationTime = Instant.now().plusSeconds(60))

        val memberValidator = OrganizationMember(
            id = "m-ref", userId = validatorId, organizationId = orgId,
            role = Role.MEMBER, status = MemberStatus.ACTIVE
        )

        every { authCodeRepository.findValidByCode(codeStr) } returns validAuthCode
        every { orgMemberRepository.findByUserIdAndOrganizationId(validatorId, orgId) } returns memberValidator

        // ACT & ASSERT
        assertThrows<IllegalAccessException> {
            authCodeService.validateCode(codeStr, validatorId)
        }
    }

    @Test
    fun `validateCode should fail when validator is NOT found in organization`() {
        // ARRANGE
        val codeStr = "123456"
        val validatorId = "unknown-id"
        val orgId = "org-id"
        val validAuthCode = AuthCode(code = codeStr, userId = "u", organizationId = orgId, expirationTime = Instant.now().plusSeconds(60))

        every { authCodeRepository.findValidByCode(codeStr) } returns validAuthCode
        // Retorna NULL
        every { orgMemberRepository.findByUserIdAndOrganizationId(validatorId, orgId) } returns null

        // ACT & ASSERT
        assertThrows<IllegalAccessException> {
            authCodeService.validateCode(codeStr, validatorId)
        }
    }
}