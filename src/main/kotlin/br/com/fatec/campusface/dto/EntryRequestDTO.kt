package br.com.fatec.campusface.dto

import br.com.fatec.campusface.models.RequestStatus
import br.com.fatec.campusface.models.Role

data class EntryRequestCreateDTO(
    val hubCode: String,
    val role: Role
)

data class EntryRequestResponseDTO(
    val id: String,
    val hubCode: String,
    val role: Role,
    val status: RequestStatus,
    val requestedAt: String, // ou Instant
    val user: UserDTO // Dados completos do solicitante para o Admin ver a foto
)