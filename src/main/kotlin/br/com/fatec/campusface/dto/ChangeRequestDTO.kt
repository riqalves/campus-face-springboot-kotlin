package br.com.fatec.campusface.dto


import br.com.fatec.campusface.models.RequestStatus
import java.time.Instant

data class ChangeRequestResponseDTO(
    val id: String,
    val status: RequestStatus,
    val requestedAt: Instant,

    val newFaceImageId: String,

    val user: UserDTO
)
