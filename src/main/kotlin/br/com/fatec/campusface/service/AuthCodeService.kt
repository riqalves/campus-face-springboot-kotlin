package br.com.fatec.campusface.service

import br.com.fatec.campusface.models.AuthCode
import br.com.fatec.campusface.models.OrganizationMember
import br.com.fatec.campusface.repository.AuthCodeRepository
import br.com.fatec.campusface.repository.OrganizationMemberRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.random.Random

@Service
class AuthCodeService(
    private val authCodeRepository: AuthCodeRepository,
    private val orgMemberRepository: OrganizationMemberRepository
) {

    /**
     * Gera um novo código de uso único para um membro da organização.
     */
    fun generateCode(orgMemberId: String): AuthCode {
        authCodeRepository.invalidatePreviousCodes(orgMemberId)

        //gerar código aletorio (6 digitos)
        val code = (100000..999999).random().toString()

        // tempo pra expirar (5 minutos)
        val expirationTime = Instant.now().plus(5, ChronoUnit.MINUTES)

        val newAuthCode = AuthCode(
            code = code,
            organizationMemberId = orgMemberId,
            expirationTime = expirationTime
        )
        return authCodeRepository.save(newAuthCode)
    }

    /**
     * Valida um código escaneado.
     * Retorna o OrganizationMember se o código for válido, ou lança uma exceção se for inválido.
     */
    fun validateCode(code: String): OrganizationMember {
        // 1. Busca o código que seja VÁLIDO no banco de dados
        val authCode = authCodeRepository.findValidByCode(code)
            ?: throw IllegalArgumentException("Código inválido, expirado ou já utilizado.")

        // 2. Com a nova consulta, não precisamos mais verificar 'isValid' aqui.
        //    Só precisamos verificar a expiração.
        if (Instant.now().isAfter(authCode.expirationTime)) {
            authCodeRepository.invalidateCode(authCode.id) // Boa prática invalidar mesmo assim
            throw IllegalStateException("Este código expirou.")
        }

        // 3. Se chegou até aqui, o código é válido. Invalida para que não possa ser usado de novo.
        authCodeRepository.invalidateCode(authCode.id)

        // 4. Retorna os detalhes do membro
        return orgMemberRepository.findById(authCode.organizationMemberId)
            ?: throw IllegalStateException("Membro da organização associado a este código não foi encontrado.")
    }
}