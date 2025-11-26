package br.com.fatec.campusface.dto


import br.com.fatec.campusface.models.RequestStatus
import java.time.Instant

data class ChangeRequestResponseDTO(
    val id: String,
    val status: RequestStatus,
    val requestedAt: Instant,

    // O ID da NOVA foto (que está pendente de aprovação)
    // O front do Admin usará isso para gerar a URL e mostrar a imagem "Nova"
    val newFaceImageId: String,

    // Os dados do usuário que pediu a troca.
    // O front do Admin pegará o 'faceImageId' de dentro deste objeto para mostrar a foto "Atual/Antiga"
    val user: UserDTO
)
