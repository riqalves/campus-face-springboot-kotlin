package br.com.fatec.campusface.service

import br.com.fatec.campusface.dto.EntryRequestCreateDTO
import br.com.fatec.campusface.dto.EntryRequestResponseDTO
import br.com.fatec.campusface.dto.UserDTO
import br.com.fatec.campusface.models.EntryRequest
import br.com.fatec.campusface.models.MemberStatus
import br.com.fatec.campusface.models.OrganizationMember
import br.com.fatec.campusface.models.RequestStatus
import br.com.fatec.campusface.models.Role
import br.com.fatec.campusface.models.User
import br.com.fatec.campusface.repository.UserRepository
import br.com.fatec.campusface.repository.EntryRequestRepository
import br.com.fatec.campusface.repository.OrganizationMemberRepository
import br.com.fatec.campusface.repository.OrganizationRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class EntryRequestService(
    private val entryRequestRepository: EntryRequestRepository,
    private val organizationMemberRepository: OrganizationMemberRepository,
    private val organizationRepository: OrganizationRepository,
    private val userRepository: UserRepository,
    private val cloudinaryService: CloudinaryService
) {


    /**
     * Cria um novo pedido de entrada.
     * O status inicial é "PENDING"
     */
    fun createRequest(userId: String, data: EntryRequestCreateDTO): EntryRequestResponseDTO {
        //busca org por hubCode
        val organization = organizationRepository.findByHubCode(data.hubCode)
            ?: throw IllegalArgumentException("Organização não encontrada com o código: ${data.hubCode}")

        // verifica se o usuario ja é um membro
        val existingMember = organizationMemberRepository.findByUserIdAndOrganizationId(userId, organization.id)
        if (existingMember != null) {
            throw IllegalStateException("Você já é um membro desta Organização")
        }
        val newEntryRequest = EntryRequest(
            userId = userId,
            organizationId = organization.id,
            hubCode = organization.hubCode,
            role = data.role,
            status = RequestStatus.PENDING,
            requestedAt = Instant.now()
        )

        val savedRequest = entryRequestRepository.save(newEntryRequest)

        val user = userRepository.findById(userId) ?: throw java.lang.IllegalStateException("Usuário não encontrado")
        return toResponseDTO(savedRequest, user)
    }

    private fun toResponseDTO(entryRequest: EntryRequest, user: User): EntryRequestResponseDTO {
        val faceUrl = user.faceImageId?.let { cloudinaryService.generateSignedUrl(it) }
        val userDTO = UserDTO.fromEntity(user, faceUrl)

        return EntryRequestResponseDTO(
            id = entryRequest.id,
            hubCode = entryRequest.hubCode,
            role = entryRequest.role,
            status = entryRequest.status,
            requestedAt = entryRequest.requestedAt,
            user = userDTO,
        )
    }

    fun listPendingRequests(userId: String): List<EntryRequestResponseDTO> {
        val organization = organizationRepository.findByHubCode(userId)
            ?: throw IllegalArgumentException("Hub não encontrado")
        val requests = entryRequestRepository.findByOrganizationIdAndStatus(organization.id, RequestStatus.PENDING)

        return requests.mapNotNull { req ->
            val user = userRepository.findById(req.userId)
            user?.let { toResponseDTO(req, it)}
        }

    }

    fun approveRequest(requestId: String){
        val request = entryRequestRepository.findById(requestId)
            ?: throw IllegalArgumentException("Solicitação não encontrado")
        if (request.status == RequestStatus.PENDING) {
            throw IllegalStateException("Esta solicitação já foi processada")
        }

        val newMember = OrganizationMember(
            organizationId = request.organizationId,
            userId = request.userId,
            role = request.role,
            status = MemberStatus.ACTIVE,
            faceImageId = null
        )

        organizationMemberRepository.save(newMember)

        entryRequestRepository.updateStatus(request.id, RequestStatus.APPROVED)

        //TODO Gatilho de SYNC
        if (request.role == Role.MEMBER) {
            println("TODO: CHAMAR O SYNC SEVICE para notificar o client sobre o membro ${newMember.id}")

        }
    }

    fun rejectRequest(requestId: String) {
        val request = entryRequestRepository.findById(requestId)
            ?: throw IllegalArgumentException("Solicitação não encontrada")

        if (request.status != RequestStatus.PENDING) {
            throw IllegalStateException("Solicitação não está pendente.")
        }

        entryRequestRepository.updateStatus(requestId, RequestStatus.DENIED)
    }

}
