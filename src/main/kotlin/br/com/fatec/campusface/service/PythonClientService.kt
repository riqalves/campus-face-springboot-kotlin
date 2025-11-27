package br.com.fatec.campusface.service

import br.com.fatec.campusface.models.RegisteredClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.stereotype.Service
import java.io.IOException

@Service
class PythonClientService(
    private val httpClient: OkHttpClient
) {

    /**
     * Verifica se o Totem Python está respondendo.
     * Endpoint: GET /api/health
     */
    fun checkHealth(client: RegisteredClient): Boolean {
        if (client.ipAddress.isBlank() || client.port.isBlank()) return false

        val url = "http://${client.ipAddress}:${client.port}/api/health"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        return try {
            httpClient.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            // Log de erro de conexão (Timeout, Host Unreachable)
            println("ERRO Conexão Python (${client.name}): ${e.message}")
            false
        }
    }

    /**
     * Envia uma nova face para ser registrada no banco vetorial do Python.
     * Endpoint: POST /api/create
     */
    fun createFace(client: RegisteredClient, userId: String, imageBytes: ByteArray): Boolean {
        val url = "http://${client.ipAddress}:${client.port}/api/create"
        return sendMultipartRequest(url, userId, imageBytes)
    }

    /**
     * Atualiza uma face existente no Python.
     * Endpoint: POST /api/update
     */
    fun updateFace(client: RegisteredClient, userId: String, imageBytes: ByteArray): Boolean {
        val url = "http://${client.ipAddress}:${client.port}/api/update"
        return sendMultipartRequest(url, userId, imageBytes)
    }

    // --- Método Privado Auxiliar para evitar duplicação ---

    private fun sendMultipartRequest(url: String, userId: String, imageBytes: ByteArray): Boolean {
        try {
            // Monta o corpo da requisição como se fosse um formulário HTML com anexo
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("user_id", userId) // O Python espera receber 'user_id'
                .addFormDataPart(
                    "file",
                    "face.jpg", // Nome do arquivo
                    imageBytes.toRequestBody("image/jpeg".toMediaType())
                )
                .build()

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    println("FALHA Python em $url - Code: ${response.code}")
                     println("Erro Body: ${response.body?.string()}")
                }
                return response.isSuccessful
            }
        } catch (e: Exception) {
            println("ERRO ao enviar para Python ($url): ${e.message}")
            return false
        }
    }
}