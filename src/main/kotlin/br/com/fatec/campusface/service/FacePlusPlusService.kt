package br.com.fatec.campusface.service

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.cloudinary.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException

@Service
class FacePlusPlusService(
    private val httpClient: OkHttpClient // Injeta o cliente HTTP
) {
    @Value("\${faceplusplus.api.key}")
    private lateinit var apiKey: String

    @Value("\${faceplusplus.api.secret}")
    private lateinit var apiSecret: String

    private val compareApiUrl = "https://api-us.faceplusplus.com/facepp/v3/compare"
    private val confidenceThreshold = 90.0 // Limiar de confiança (0 a 100). Ajuste conforme necessário.

    fun facesMatch(referenceImageUrl: String, newImageFile: MultipartFile): Boolean {
        return try {
            // ... (o código para baixar a imagem e montar a requisição continua o mesmo)
            val referenceImageBytes = downloadImageFromUrl(referenceImageUrl)

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("api_key", apiKey)
                .addFormDataPart("api_secret", apiSecret)
                .addFormDataPart(
                    "image_file1",
                    "reference.jpg",
                    referenceImageBytes.toRequestBody("image/jpeg".toMediaType())
                )
                .addFormDataPart(
                    "image_file2",
                    newImageFile.originalFilename,
                    newImageFile.bytes.toRequestBody(newImageFile.contentType?.toMediaType())
                )
                .build()

            val request = Request.Builder()
                .url(compareApiUrl)
                .post(requestBody)
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    println("ERRO Face++: Código ${response.code} - ${response.body?.string()}")
                    return false
                }

                val responseBody = response.body!!.string()
                val jsonObject = JSONObject(responseBody)

                println("DEBUG - Resposta da API Face++: $responseBody")

                // --- CORREÇÃO AQUI ---
                // 1. Verifica se a resposta contém o campo 'confidence'.
                if (jsonObject.has("confidence")) {
                    // 2. Se tiver, extrai o valor e faz a comparação.
                    val confidence = jsonObject.getDouble("confidence")
                    println("DEBUG - Confiança da verificação (Face++): $confidence")
                    return confidence >= confidenceThreshold
                } else {
                    // 3. Se não tiver, significa que não foi possível comparar (ex: rosto não encontrado).
                    //    Nesse caso, a verificação falhou.
                    println("DEBUG - Campo 'confidence' não encontrado na resposta. Provavelmente um rosto não foi detectado.")
                    return false
                }
                // --- FIM DA CORREÇÃO ---
            }
        } catch (e: Exception) {
            println("ERRO - Falha na verificação facial com Face++: ${e.message}")
            e.printStackTrace()
            return false
        }
    }

    private fun downloadImageFromUrl(url: String): ByteArray {
        val request = Request.Builder().url(url).build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Falha ao baixar imagem do Cloudinary: $response")
            }
            return response.body!!.bytes()
        }
    }
}