package br.com.fatec.campusface.dto

import br.com.fatec.campusface.models.RequestStatus
import br.com.fatec.campusface.models.Role
import java.time.Instant

data class EntryRequestCreateDTO(
    val hubCode: String,
    val role: Role
)

data class EntryRequestResponseDTO(
    val id: String,
    val hubCode: String,
    val role: Role,
    val status: RequestStatus,
    val requestedAt: Instant, // ou Instant
    val user: UserDTO // Dados completos do solicitante para o Admin ver a foto
)