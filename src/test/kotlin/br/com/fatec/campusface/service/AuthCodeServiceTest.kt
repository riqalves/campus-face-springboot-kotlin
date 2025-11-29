package br.com.fatec.campusface.service

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

// [DRIVER] A classe JUnit atua como driver, conduzindo a execução dos testes.
class AuthCodeServiceTest {

    // [MOCK] Objetos simulados que substituem as dependências reais (Banco de Dados).
    // Usamos MockK para isolar a lógica do Service.
    @MockK
    lateinit var authCodeRepository: AuthCodeRepository

    @MockK
    lateinit var orgMemberRepository: OrganizationMemberRepository

    @MockK
    lateinit var memberService: OrganizationMemberService

    // Injeta os Mocks acima dentro do Service que será testado.
    @InjectMockKs
    lateinit var authCodeService: AuthCodeService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this) // Inicializa os mocks antes de cada teste
    }

    @Test
    fun `generateCode deve criar novo codigo e invalidar anteriores`() {
        // --- ARRANGE (Preparação) ---
        val userId = "user123"
        val orgId = "orgABC"
        val activeMember = OrganizationMember(userId = userId, organizationId = orgId, status = MemberStatus.ACTIVE)

        // [STUB] Configurando o comportamento esperado dos mocks (Stubbing).
        // Quando o repositório for chamado, ele retornará o objeto 'activeMember' que criamos.
        every { orgMemberRepository.findByUserIdAndOrganizationId(userId, orgId) } returns activeMember
        every { authCodeRepository.invalidatePreviousCodes(userId, orgId) } just Runs
        every { authCodeRepository.save(any()) } returnsArgument 0

        // --- ACT (Ação) ---
        val result = authCodeService.generateCode(userId, orgId)

        // --- ASSERT (Verificação) ---
        assertNotNull(result.code)
        assertEquals(6, result.code.length)

        // Verifica se a interação com o Mock ocorreu (se o método foi chamado)
        verify(exactly = 1) { authCodeRepository.invalidatePreviousCodes(userId, orgId) }
    }

    @Test
    fun `validateCode deve falhar se codigo estiver expirado`() {
        // --- ARRANGE ---
        // [STUB] Simulando um código que já expirou no banco de dados
        val expiredCode = AuthCode(
            id = "expiredId",
            expirationTime = Instant.now().minus(1, ChronoUnit.SECONDS), // Expirou
            valid = true
        )

        every { authCodeRepository.findValidByCode("123456") } returns expiredCode
        every { authCodeRepository.invalidateCode("expiredId") } just Runs

        // --- ACT ---
        val result = authCodeService.validateCode("123456", "fiscal")

        // --- ASSERT ---
        assertFalse(result.valid)
        assertEquals("Código expirado.", result.message)

        // Verifica se o sistema tentou invalidar o código expirado no banco
        verify(exactly = 1) { authCodeRepository.invalidateCode("expiredId") }
    }

    @Test
    fun `validateCode deve lancar erro de seguranca se fiscal nao tiver permissao`() {
        // --- ARRANGE ---
        val orgId = "orgFatec"
        val validAuthCode = AuthCode(organizationId = orgId, expirationTime = Instant.now().plusSeconds(60))

        // Fiscal sem permissão (apenas MEMBER)
        val unauthorizedMember = OrganizationMember(role = Role.MEMBER, status = MemberStatus.ACTIVE)

        every { authCodeRepository.findValidByCode("123456") } returns validAuthCode
        every { orgMemberRepository.findByUserIdAndOrganizationId("alunoIntruso", orgId) } returns unauthorizedMember

        // --- ACT & ASSERT ---
        // Valida se a exceção correta é lançada
        assertThrows<IllegalAccessException> {
            authCodeService.validateCode("123456", "alunoIntruso")
        }
    }

    @Test
    fun `validateCode deve retornar erro se o usuario dono do codigo nao for encontrado na org`() {
        // ARRANGE
        val codeStr = "123456"
        val validatorId = "fiscal01"
        val orgId = "orgFatec"
        val targetUserId = "alunoFantasma"

        val validAuthCode = AuthCode(
            id = "codeId1", code = codeStr, userId = targetUserId, organizationId = orgId,
            expirationTime = Instant.now().plusSeconds(60), valid = true
        )

        // Fiscal tem permissão
        val validatorMember = OrganizationMember(userId = validatorId, role = Role.VALIDATOR, status = MemberStatus.ACTIVE)

        // Mocks
        every { authCodeRepository.findValidByCode(codeStr) } returns validAuthCode
        every { orgMemberRepository.findByUserIdAndOrganizationId(validatorId, orgId) } returns validatorMember
        every { authCodeRepository.invalidateCode(validAuthCode.id) } just Runs

        every { orgMemberRepository.findByUserIdAndOrganizationId(targetUserId, orgId) } returns null

        // ACT
        val result = authCodeService.validateCode(codeStr, validatorId)

        // ASSERT
        assertFalse(result.valid)
        assertEquals("Usuário do código não encontrado na organização.", result.message)

        // Confirma que o código foi queimado (pois tecnicamente era válido, só o usuário que sumiu)
        verify(exactly = 1) { authCodeRepository.invalidateCode(validAuthCode.id) }
    }
}