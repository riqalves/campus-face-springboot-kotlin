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
     * Endpoint: GET /api/health (ou apenas checagem de conexão)
     */
    fun checkHealth(client: RegisteredClient): Boolean {
        if (client.ipAddress.isBlank() || client.port.isBlank()) return false

        // Tenta bater na raiz ou em um endpoint leve
        val url = "http://${client.ipAddress}:${client.port}/"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        return try {
            httpClient.newCall(request).execute().use { response ->
                // Aceitamos qualquer resposta (200, 404, 500) como sinal de que o servidor está vivo e alcançável
                true
            }
        } catch (e: Exception) {
            println("WARN PythonClient: Falha ao conectar em ${client.name} (${client.ipAddress}): ${e.message}")
            false
        }
    }

    /**
     * Envia atualização para o endpoint /upsert do Python.
     * Serve tanto para criar quanto para atualizar (Protocolo do seu amigo).
     */
    fun upsertFace(client: RegisteredClient, userId: String, imageBytes: ByteArray): Boolean {
        // Monta a URL (ex: http://192.168.1.10:3000/upsert)
        val url = "http://${client.ipAddress}:${client.port}/upsert"

        try {
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                // Nomes dos campos conforme especificação do client Python:
                .addFormDataPart("Id_user", userId)
                .addFormDataPart(
                    "image", // Nome do campo de arquivo
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
                    println("FALHA Python Upsert em $url - Code: ${response.code}")
                    // Opcional: Ler o body de erro para debug
                    // println("Erro Body: ${response.body?.string()}")
                }
                return response.isSuccessful
            }
        } catch (e: Exception) {
            println("ERRO ao enviar Upsert para Python ($url): ${e.message}")
            return false
        }
    }

    // --- Métodos Legados (Caso ainda precise usar os endpoints antigos separados) ---

    fun createFace(client: RegisteredClient, userId: String, imageBytes: ByteArray): Boolean {
        val url = "http://${client.ipAddress}:${client.port}/api/create"
        return sendLegacyMultipartRequest(url, userId, imageBytes)
    }

    fun updateFace(client: RegisteredClient, userId: String, imageBytes: ByteArray): Boolean {
        val url = "http://${client.ipAddress}:${client.port}/api/update"
        return sendLegacyMultipartRequest(url, userId, imageBytes)
    }

    private fun sendLegacyMultipartRequest(url: String, userId: String, imageBytes: ByteArray): Boolean {
        try {
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("user_id", userId)
                .addFormDataPart(
                    "file",
                    "face.jpg",
                    imageBytes.toRequestBody("image/jpeg".toMediaType())
                )
                .build()

            val request = Request.Builder().url(url).post(requestBody).build()

            httpClient.newCall(request).execute().use { response ->
                return response.isSuccessful
            }
        } catch (e: Exception) {
            return false
        }
    }
}