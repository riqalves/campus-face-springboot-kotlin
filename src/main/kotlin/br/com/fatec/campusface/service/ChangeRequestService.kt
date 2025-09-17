package br.com.fatec.campusface.service

import br.com.fatec.campusface.models.ChangeRequest
import br.com.fatec.campusface.repository.ChangeRequestRepository
import br.com.fatec.campusface.repository.OrganizationMemberRepository
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.lang.IllegalStateException

@Service
class ChangeRequestService(
    private val changeRequestRepository: ChangeRequestRepository,
    private val orgMemberRepository: OrganizationMemberRepository,
    private val cloudinaryService: CloudinaryService
) {

    /**
     * Lógica para um MEMBRO criar uma solicitação de mudança de foto.
     */
    fun createChangeRequest(orgMemberId: String, newImageFile: MultipartFile): ChangeRequest {
        // 1. Busca o registro do membro da organização
        val orgMember = orgMemberRepository.findById(orgMemberId)
            ?: throw IllegalStateException("Registro de membro da organização não encontrado.")

        // 2. Faz o upload da NOVA imagem para o Cloudinary
        val uploadResult = cloudinaryService.upload(newImageFile)
        val newImagePublicId = uploadResult["public_id"]
            ?: throw IllegalStateException("Public ID não retornado pelo Cloudinary.")

        // 3. Cria a solicitação de mudança
        val newRequest = ChangeRequest(
            organizationId = orgMember.organizationId,
            organizationMemberId = orgMemberId,
            newFaceImagePublicId = newImagePublicId
        )

        // 4. Salva a solicitação no banco de dados
        return changeRequestRepository.save(newRequest)
    }

    /**
     * Lógica para um ADMIN aprovar ou negar uma solicitação.
     */
    fun reviewChangeRequest(requestId: String, approve: Boolean) {
        // 1. Busca a solicitação
        val request = changeRequestRepository.findById(requestId)
            ?: throw IllegalStateException("Solicitação de mudança não encontrada.")

        if (approve) {
            // --- LÓGICA DE APROVAÇÃO ---
            // 2a. Busca o registro do membro
            val orgMember = orgMemberRepository.findById(request.organizationMemberId)
                ?: throw IllegalStateException("Membro da organização associado à solicitação não foi encontrado.")

            val oldImagePublicId = orgMember.faceImageId

            // 3a. Atualiza o registro do membro com o ID da nova imagem
            orgMemberRepository.updateFaceImageId(request.organizationMemberId, request.newFaceImagePublicId)

            // 4a. (Opcional, mas recomendado) Deleta a imagem ANTIGA do Cloudinary
            if (!oldImagePublicId.isNullOrEmpty()) {
                cloudinaryService.delete(oldImagePublicId)
            }
        } else {
            // --- LÓGICA DE NEGAÇÃO ---
            // 2b. Apenas deleta a imagem NOVA que foi enviada e nunca será usada
            cloudinaryService.delete(request.newFaceImagePublicId)
        }

        // 5. Deleta a solicitação, pois ela já foi processada
        changeRequestRepository.delete(requestId)
    }
}