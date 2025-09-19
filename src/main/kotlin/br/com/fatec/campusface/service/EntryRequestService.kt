package br.com.fatec.campusface.service

import br.com.fatec.campusface.dto.EntryRequestDTO
import br.com.fatec.campusface.models.EntryRequest
import br.com.fatec.campusface.models.OrganizationMember
import br.com.fatec.campusface.models.Role
import br.com.fatec.campusface.repository.UserRepository
import br.com.fatec.campusface.repository.EntryRequestRepository
import br.com.fatec.campusface.repository.OrganizationMemberRepository
import br.com.fatec.campusface.repository.OrganizationRepository
import org.springframework.stereotype.Service

@Service
class EntryRequestService(
    private val entryRequestRepository: EntryRequestRepository,
    private val organizationMemberRepository: OrganizationMemberRepository,
    private val organizationRepository: OrganizationRepository,
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val facePlusPlusService: FacePlusPlusService,
    private val cloudinaryService: CloudinaryService
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
        val fullRequest = entryRequestRepository.findById(entryRequest.id)
            ?: throw IllegalArgumentException("Pedido de entrada não encontrado")

        if (fullRequest.status != "WAITING") {
            throw IllegalStateException("A solicitação já foi processada")
        }

        val user = userRepository.findById(fullRequest.user?.id!!)
            ?: throw IllegalStateException("Usuário não encontrado.")

        val organization = organizationRepository.findById(fullRequest.organizationId)
            ?: throw IllegalStateException("Organização não encontrada.")

        // Cria o registro de OrganizationMember (continua útil para rastrear a afiliação)
        val newMember = OrganizationMember(
            userId = fullRequest.user.id,
            organizationId = fullRequest.organizationId,
            faceImageId = user.faceImageId
        )
        val savedMember = organizationMemberRepository.save(newMember)

        // LÓGICA CONDICIONAL BASEADA NA ROLE DO USUÁRIO
        when (user.role) {
            Role.MEMBER -> {
                println("DEBUG - Aprovando um MEMBER.")
                // CORREÇÃO: Passa o ID do USUÁRIO, não do OrganizationMember
                organizationRepository.addMemberToOrganization(fullRequest.organizationId, user.id)

                // ... (lógica de registro facial, que já usa o 'user' e está correta)
                val userImagePublicId = user.faceImageId ?: throw IllegalStateException("Membro não tem imagem de referência.")
                val faceSetToken = organization.faceSetToken ?: throw IllegalStateException("Organização não configurada para reconhecimento facial.")
                val imageBytes = cloudinaryService.downloadImageFromUrl(cloudinaryService.generateSignedUrl(userImagePublicId))
                val faceToken = facePlusPlusService.detectFaceAndGetToken(imageBytes)
                userRepository.updateFaceToken(user.id, faceToken)
                println("DEBUG - Pausando por 1.1 segundos para evitar limite de concorrência...")
                Thread.sleep(1100) // 1100 milissegundos
                println("DEBUG - Tentando adicionar faceToken: [$faceToken] ao faceSetToken: [$faceSetToken]")
                facePlusPlusService.addFaceToFaceSet(faceToken, faceSetToken)
                // ADICIONE ESTES LOGS PARA VERIFICAR OS TOKENS

            }

            Role.VALIDATOR -> {
                println("DEBUG - Aprovando um VALIDATOR.")
                // CORREÇÃO: Passa o ID do USUÁRIO, não do OrganizationMember
                organizationRepository.addValidatorToOrganization(fullRequest.organizationId, user.id)
            }

            Role.ADMIN -> {
                println("DEBUG - Aprovando um ADMIN.")
                // CORREÇÃO: Passa o ID do USUÁRIO, não do OrganizationMember
                organizationRepository.addAdminToOrganization(fullRequest.organizationId, user.id)
            }
        }

        entryRequestRepository.updateStatus(fullRequest.id, "APPROVED")

        return savedMember
    }

    /**
     * Rejeita um pedido de entrada
     */
    fun rejectRequest(requestId: String) {
        // 1. Busca a solicitação completa do banco de dados.
        val request = entryRequestRepository.findById(requestId)
            ?: throw IllegalArgumentException("Pedido de entrada não encontrado (id=$requestId)")

        // 2. Garante que a solicitação ainda está pendente.
        if (request.status != "WAITING") {
            throw IllegalStateException("Pedido já foi processado (status é ${request.status})")
        }

        // 3. Chama o método de atualização simples, passando o ID e o novo status.
        entryRequestRepository.updateStatus(request.id, "DENIED")

        println("DEBUG - Pedido ${request.id} rejeitado com sucesso.")
        // Este método não precisa retornar nada, pois ele apenas realiza uma ação.
    }


    /**
     * Busca um pedido específico
     */
    fun getRequestById(requestId: String): EntryRequestDTO? {
        return entryRequestRepository.findById(requestId)
    }


}
