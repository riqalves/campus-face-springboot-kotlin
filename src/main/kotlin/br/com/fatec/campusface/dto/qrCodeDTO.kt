package br.com.fatec.campusface.dto
import java.time.Instant

data class GenerateCodeRequest(
    val organizationId: String // O membro escolhe para qual Org quer gerar o passe
)

data class GeneratedCodeResponse(
    val code: String,
    val expirationTime: Instant
)

data class ValidateCodeRequest(
    val code: String
)

data class ValidationResponseDTO(
    val valid: Boolean,
    val message: String,
    val member: OrganizationMemberDTO? // Retorna os dados completos (foto, cargo) para o fiscal conferir
)