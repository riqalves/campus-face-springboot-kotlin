package br.com.fatec.campusface.models

import br.com.fatec.campusface.dto.UserDTO
import java.time.Instant

data class EntryRequestCreateDTO(
    val hubCode: String,
    val role: Role
)

data class EntryRequestResponseDTO(
    val id:String = "",
    val status: RequestStatus,
    val role: Role,
    val hubCode: String,
    val user: UserDTO,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)