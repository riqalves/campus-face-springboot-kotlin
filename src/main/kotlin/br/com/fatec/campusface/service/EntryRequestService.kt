package br.com.fatec.campusface.service

import br.com.fatec.campusface.dto.EntryRequestDTO
import br.com.fatec.campusface.models.EntryRequest
import br.com.fatec.campusface.models.OrganizationMember
import br.com.fatec.campusface.repository.EntryRequestRepository
import br.com.fatec.campusface.repository.OrganizationMemberRepository
import br.com.fatec.campusface.repository.OrganizationRepository
import org.springframework.stereotype.Service

@Service
class EntryRequestService(
    private val entryRequestRepository: EntryRequestRepository,
    private val organizationMemberRepository: OrganizationMemberRepository,
    private val organizationRepository: OrganizationRepository,
    private val userService: UserService
) {


    /**
     * Cria um novo pedido de entrada.
     * O status inicial é "WAITING"
     */
    fun createRequest(entryRequest: EntryRequest): EntryRequestDTO {
        if (entryRequest.userId == null) {
            throw (IllegalArgumentException("User ID is required"))
        }
        return entryRequestRepository.save(entryRequest)
    }

    /**
     * Lista todos os pedidos de uma organização (útil para ADMINs)
     */
    fun getRequestsByOrganization(organizationId: String): List<EntryRequestDTO> {
        return entryRequestRepository.findByOrganizationId(organizationId)
    }



    /**
     * Aprova um pedido de entrada, criando o OrganizationMember
     */
    fun approveRequest(entryRequest: EntryRequest): OrganizationMember? {
        val request = entryRequestRepository.findById(entryRequest.id)
            ?: throw IllegalArgumentException("Pedido de entrada não encontrado")

        if (request.status != "WAITING") {
            throw IllegalStateException("A solicitação já foi processada")
        }

        // 1. Atualiza o status do pedido para "APPROVED"
        entryRequestRepository.updateStatus(entryRequest)

        // 2. Busca os detalhes do usuário para obter o public_id da imagem
        val user = userService.getUserById(entryRequest.userId!!)
            ?: throw IllegalStateException("Usuário associado ao pedido não encontrado")

        // 3. Cria o novo registro de OrganizationMember
        val newMember = OrganizationMember(
            userId =  entryRequest.userId,
            organizationId = request.organizationId,
            faceImageId = user.faceImageId
        )
        val savedMember = organizationMemberRepository.save(newMember)

        // 4. Adiciona o ID do novo membro à lista de 'memberIds' da organização
        organizationRepository.addMemberToOrganization(request.organizationId, savedMember.id)

        println("DEBUG - Membro ${savedMember.id} adicionado à organização ${request.organizationId}")

        return savedMember
    }

    /**
     * Rejeita um pedido de entrada
     */
    fun rejectRequest(entryRequest: EntryRequest): EntryRequestDTO {
        val request = entryRequestRepository.findById(entryRequest.id)
            ?: throw IllegalArgumentException("Pedido de entrada não encontrado (id=$entryRequest)")

        if (request.status != "WAITING") {
            throw IllegalStateException("Pedido já foi processado (status é ${request.status})")
        }

        val updated = entryRequestRepository.updateStatus(entryRequest)

        println("DEBUG - Pedido rejeitado: $updated")
        return updated
    }


    /**
     * Busca um pedido específico
     */
    fun getRequestById(requestId: String): EntryRequestDTO? {
        return entryRequestRepository.findById(requestId)
    }
}
