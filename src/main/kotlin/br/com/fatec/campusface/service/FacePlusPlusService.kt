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
    private val httpClient: OkHttpClient,
    private val imageProcessingService: ImageProcessingService
) {
    @Value("\${faceplusplus.api.key}")
    private lateinit var apiKey: String

    @Value("\${faceplusplus.api.secret}")
    private lateinit var apiSecret: String

    private val compareApiUrl = "https://api-us.faceplusplus.com/facepp/v3/compare"
    private val confidenceThreshold = 90.0

    fun facesMatch(referenceImageUrl: String?, newImageFile: MultipartFile): Boolean {
        if (referenceImageUrl.isNullOrBlank()) {
            println("ERRO - A URL da imagem de referência está vazia.")
            return false
        }

        return try {
            val referenceImageBytes = downloadImageFromUrl(referenceImageUrl)

            val processedNewImageBytes = imageProcessingService.processImageForApi(newImageFile)

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("api_key", apiKey)
                .addFormDataPart("api_secret", apiSecret)
                .addFormDataPart(
                    "image_file1", // Imagem de referência já otimizada
                    "reference.jpg",
                    referenceImageBytes.toRequestBody("image/jpeg".toMediaType())
                )
                .addFormDataPart(
                    "image_file2", // Nova imagem AGORA otimizada
                    "new_image.jpg",
                    processedNewImageBytes.toRequestBody("image/jpeg".toMediaType())
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

                if (jsonObject.has("confidence")) {
                    val confidence = jsonObject.getDouble("confidence")
                    println("DEBUG - Confiança da verificação (Face++): $confidence")
                    return confidence >= confidenceThreshold
                } else {
                    println("DEBUG - Campo 'confidence' não encontrado na resposta. Provavelmente um rosto não foi detectado.")
                    return false
                }
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