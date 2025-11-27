package br.com.fatec.campusface.service

import br.com.fatec.campusface.models.ClientStatus
import br.com.fatec.campusface.repository.RegisteredClientRepository
import br.com.fatec.campusface.repository.UserRepository
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class SyncService(
    private val registeredClientRepository: RegisteredClientRepository,
    private val userRepository: UserRepository,
    private val cloudinaryService: CloudinaryService,
    private val pythonClientService: PythonClientService
) {

    /**
     * Notifica os totens que um novo membro foi aprovado.
     * Deve ser chamado pelo EntryRequestService.approveRequest().
     * * @Async é recomendado aqui para não travar a resposta HTTP do Admin enquanto fazemos upload para N totens.
     * (Requer @EnableAsync na classe Application)
     */
    @Async
    fun notifyNewMember(organizationId: String, userId: String) {
        println("SYNC - Iniciando sincronização de NOVO MEMBRO: $userId para Org: $organizationId")
        syncFaceToClients(organizationId, userId, isUpdate = false)
    }

    /**
     * Notifica os totens que um membro atualizou sua foto.
     * Deve ser chamado pelo UserController.updateProfileImage() (se o usuário for membro).
     */
    @Async
    fun notifyMemberUpdate(organizationId: String, userId: String) {
        println("SYNC - Iniciando sincronização de UPDATE MEMBRO: $userId para Org: $organizationId")
        syncFaceToClients(organizationId, userId, isUpdate = true)
    }

    private fun syncFaceToClients(organizationId: String, userId: String, isUpdate: Boolean) {
        // 1. Busca dados do usuário
        val user = userRepository.findById(userId)
        if (user == null) {
            println("ERRO Sync: Usuário $userId não encontrado.")
            return
        }

        val faceId = user.faceImageId
        if (faceId.isNullOrBlank()) {
            println("WARN Sync: Usuário $userId não tem foto cadastrada. Ignorando sync.")
            return
        }

        // 2. Busca todos os totens ONLINE dessa organização
        // Nota: Utilizando o método existente no repositório (com o typo 'Organizaiton' mantido)
        val clients = registeredClientRepository.findByOrganizaitonIdAndStatus(organizationId, ClientStatus.ONLINE)

        if (clients.isEmpty()) {
            println("INFO Sync: Nenhum totem online para a organização $organizationId.")
            return
        }

        // 3. Baixa a imagem do Cloudinary (Bytes) UMA vez para replicar para N totens
        val url = cloudinaryService.generateSignedUrl(faceId)
        val imageBytes: ByteArray
        try {
            imageBytes = cloudinaryService.downloadImageFromUrl(url)
        } catch (e: Exception) {
            println("ERRO Sync: Falha ao baixar imagem do Cloudinary: ${e.message}")
            return
        }

        // 4. Envia para cada Totem
        clients.forEach { client ->
            try {
                val success = if (isUpdate) {
                    pythonClientService.updateFace(client, userId, imageBytes)
                } else {
                    pythonClientService.createFace(client, userId, imageBytes)
                }

                if (success) {
                    println("SUCCESS Sync: Face sincronizada com o totem ${client.name} (${client.ipAddress})")
                } else {
                    println("FAIL Sync: Totem ${client.name} rejeitou a sincronização.")
                }
            } catch (e: Exception) {
                println("ERRO Sync: Falha de comunicação com totem ${client.name}: ${e.message}")
            }
        }
    }
}