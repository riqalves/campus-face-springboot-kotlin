package br.com.fatec.campusface.models

import java.time.Instant

data class ChangeRequest(
    val id: String = "",
    val organizationId: String = "",
    val organizationMemberId: String = "",
    // Armazena o public_id da NOVA imagem enviada para o Cloudinary
    val newFaceImagePublicId: String = "",
    val status: RequestStatus = RequestStatus.PENDING,
    val requestedAt: Instant = Instant.now()
)