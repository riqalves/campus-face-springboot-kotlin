package br.com.fatec.campusface.service

import br.com.fatec.campusface.models.ClientStatus
import br.com.fatec.campusface.models.RegisteredClient
import br.com.fatec.campusface.repository.RegisteredClientRepository
import br.com.fatec.campusface.repository.UserRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.io.IOException

@Service
class SyncService(
    private val registeredClientRepository: RegisteredClientRepository,
    private val userRepository: UserRepository,
    private val cloudinaryService: CloudinaryService,
    private val httpClient: OkHttpClient
) {

    /**
     * Ponto de entrada: Chamado pelo EntryRequestService ou ChangeRequestService.
     * Busca os dados e dispara o envio para todos os totens online da organização.
     */
    @Async
    fun syncNewMember(organizationId: String, userId: String) {
        val activeClients = registeredClientRepository.findByOrganizaitonIdAndStatus(organizationId, ClientStatus.ONLINE)
        println("DEBUG (Sync): Encontrados ${activeClients.size} clientes online para o Hub $organizationId")
        if (activeClients.isEmpty()) {
            println("AVISO (Sync): Nenhum cliente Python online para o Hub $organizationId")
            return
        }

        // 2. Busca o Usuário e a Foto
        val user = userRepository.findById(userId)
        if (user == null) {
            println("ERRO (Sync): Usuário $userId não encontrado.")
            return
        }

        val faceId = user.faceImageId
        if (faceId.isNullOrBlank()) {
            println("AVISO (Sync): Usuário $userId não tem foto cadastrada. Ignorando sync.")
            return
        }

        // 3. Baixa os bytes da imagem do Cloudinary
        val imageBytes: ByteArray
        try {
            val signedUrl = cloudinaryService.generateSignedUrl(faceId)
            imageBytes = cloudinaryService.downloadImageFromUrl(signedUrl)
        } catch (e: Exception) {
            println("ERRO (Sync): Falha ao baixar imagem do Cloudinary: ${e.message}")
            return
        }

        // 4. Monta o Corpo da Requisição (Multipart com ARQUIVO)
        // Isso cria o payload uma única vez para enviar a todos
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("Id_user", userId) // Nome esperado pelo Python
            .addFormDataPart(
                "image", // Nome do campo esperado pelo Python
                "face.jpg", // Nome do arquivo (OBRIGATÓRIO para ser tratado como upload)
                imageBytes.toRequestBody("image/jpeg".toMediaType())
            )
            .build()

        // 5. Envia para cada totem
        activeClients.forEach { client ->
            sendUpsertRequest(client, requestBody)
        }
    }

    /**
     * Envia a requisição HTTP para um cliente específico.
     */
    private fun sendUpsertRequest(client: RegisteredClient, requestBody: MultipartBody) {
        val url = "http://${client.ipAddress}:${client.port}/upsert"
        val request = Request.Builder().url(url).post(requestBody).build()

        try {
            println("DEBUG (Sync): Enviando face para ${client.name} ($url)...")

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    println("SUCESSO (Sync): Face sincronizada com ${client.name}.")
                } else {
                    println("ERRO (Sync): Cliente ${client.name} rejeitou (Code: ${response.code}).")
                }
            }
        } catch (e: IOException) {
            // Se der erro de rede (Timeout, Connection Refused), marca como fora do ar
            println("ERRO FATAL (Sync): Falha ao conectar com ${client.name}: ${e.message}")
            markClientAsUnreachable(client)
        } catch (e: Exception) {
            println("ERRO (Sync): Erro genérico ao enviar para ${client.name}: ${e.message}")
        }
    }

    /**
     * Marca o cliente como UNREACHABLE no banco de dados para evitar tentar enviar de novo.
     */
    private fun markClientAsUnreachable(client: RegisteredClient) {
        println("INFO (Sync): Marcando cliente ${client.name} (ID: ${client.id}) como UNREACHABLE.")
        val updatedClient = client.copy(status = ClientStatus.UNREACHABLE)
        registeredClientRepository.save(updatedClient)
    }
}