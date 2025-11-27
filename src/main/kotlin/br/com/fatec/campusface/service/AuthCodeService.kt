package br.com.fatec.campusface.service

import br.com.fatec.campusface.dto.UserDTO
import br.com.fatec.campusface.models.AuthCode
import br.com.fatec.campusface.models.User
import br.com.fatec.campusface.repository.AuthCodeRepository
import br.com.fatec.campusface.repository.OrganizationRepository
import br.com.fatec.campusface.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class AuthCodeService(
    private val authCodeRepository: AuthCodeRepository,
    private val userRepository: UserRepository,
    private val organizationRepository: OrganizationRepository,
) {

    /**
     * Gera um novo código de uso único para um membro da organização.
     */
    fun generateCode(userId: String): AuthCode {
        authCodeRepository.invalidatePreviousCodes(userId)

        val code = (100000..999999).random().toString()

        val expirationTime = Instant.now().plus(5, ChronoUnit.MINUTES)

        val newAuthCode = AuthCode(
            code = code,
            userId = userId,
            expirationTime = expirationTime
        )
        return authCodeRepository.save(newAuthCode)
    }

    /**
     * Valida um código escaneado.
     * Retorna o OrganizationMember se o código for válido, ou lança uma exceção se for inválido.
     */
    fun validateCode(code: String): UserDTO {
        val authCode = authCodeRepository.findValidByCode(code)
            ?: throw IllegalArgumentException("Código inválido, expirado ou já utilizado.")

        if (Instant.now().isAfter(authCode.expirationTime)) {
            authCodeRepository.invalidateCode(authCode.id)
            throw IllegalStateException("Este código expirou.")
        }

        authCodeRepository.invalidateCode(authCode.id)

        val user = userRepository.findById(authCode.userId)
            ?: throw IllegalStateException("Usuário associado a este código não foi encontrado.")

        return user.toDTO()
    }

    /**
     * Função de extensão privada para converter um User em um UserDTO.
     */
    private fun User.toDTO(): UserDTO {
        return UserDTO(
            id = this.id,
            fullName = this.fullName,
            email = this.email,
            document = this.document,
            faceImageId = this.faceImageId,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )
    }
}
