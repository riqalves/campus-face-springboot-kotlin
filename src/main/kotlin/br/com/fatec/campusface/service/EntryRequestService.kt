package br.com.fatec.campusface.service

import br.com.fatec.campusface.dto.EntryRequestDTO
import br.com.fatec.campusface.models.EntryRequest
import br.com.fatec.campusface.models.OrganizationMember
import br.com.fatec.campusface.repository.EntryRequestRepository
import br.com.fatec.campusface.repository.OrganizationMemberRepository
import org.springframework.stereotype.Service

@Service
class EntryRequestService(
    private val entryRequestRepository: EntryRequestRepository,
    private val organizationMemberRepository: OrganizationMemberRepository,
    private val userService: UserService
) {


    /**
     * Cria um novo pedido de entrada.
     * O status inicial é "WAITING"
     */
    fun createRequest(entryRequest: EntryRequest): EntryRequestDTO {
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
        println("DEBUG APPROVE REQUEST: $entryRequest" )
        val request = entryRequestRepository.findById(entryRequest.id)
            ?: throw IllegalArgumentException("Pedido de entrada não encontrado")

        if (request.status != "WAITING") {
            throw IllegalStateException("Solicitação já foi processada")
        }

        // Atualiza status do pedido para "APPROVED"
        val updated = entryRequestRepository.updateStatus(entryRequest)
        println("DEBUG UPDATE: $updated ")
        val user = updated.user?.id?.let { userService.getUserById(it) }
        // Cria o membro na organização
        val member = OrganizationMember(
            userId =  entryRequest.userId,
            organizationId = request.organizationId,
            faceImageId = user!!.faceImageId
        )
        println("DEBUG ENTRYREQUESTSERVICE: $member")

        return organizationMemberRepository.save(member)
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
