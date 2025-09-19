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

    private val detectApiUrl = "https://api-us.faceplusplus.com/facepp/v3/detect"
    private val faceSetCreateApiUrl = "https://api-us.faceplusplus.com/facepp/v3/faceset/create"
    private val faceSetAddFaceApiUrl = "https://api-us.faceplusplus.com/facepp/v3/faceset/addface"
    private val searchApiUrl = "https://api-us.faceplusplus.com/facepp/v3/search"


    @Value("\${faceplusplus.api.key}")
    private lateinit var apiKey: String

    @Value("\${faceplusplus.api.secret}")
    private lateinit var apiSecret: String

    private val compareApiUrl = "https://api-us.faceplusplus.com/facepp/v3/compare"
    private val confidenceThreshold = 85.0

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


    /**
     * NOVO: Cria um novo FaceSet no Face++.
     * @return O 'faceset_token' gerado pela API.
     */
    fun createFaceSet(): String {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("api_key", apiKey)
            .addFormDataPart("api_secret", apiSecret)
            .build()

        val request = Request.Builder().url(faceSetCreateApiUrl).post(requestBody).build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Falha ao criar FaceSet: ${response.body?.string()}")
            val json = JSONObject(response.body!!.string())
            return json.getString("faceset_token")
        }
    }


    /**
     * NOVO: Detecta um rosto em uma imagem e retorna seu 'face_token'.
     * @param imageBytes Os bytes da imagem.
     * @return O 'face_token' do primeiro rosto encontrado.
     */
    fun detectFaceAndGetToken(imageBytes: ByteArray): String {
        val processedBytes = imageProcessingService.processImageBytes(imageBytes)

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("api_key", apiKey)
            .addFormDataPart("api_secret", apiSecret)
            .addFormDataPart("image_file", "face.jpg", processedBytes.toRequestBody("image/jpeg".toMediaType()))
            .build()

        val request = Request.Builder().url(detectApiUrl).post(requestBody).build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Falha ao detectar rosto: ${response.body?.string()}")
            val json = JSONObject(response.body!!.string())
            val faces = json.getJSONArray("faces")
            if (faces.length() == 0) throw IllegalStateException("Nenhum rosto encontrado na imagem para gerar token.")
            return faces.getJSONObject(0).getString("face_token")
        }
    }

    /**
     * NOVO: Adiciona um rosto (identificado por seu face_token) a um FaceSet.
     */
    fun addFaceToFaceSet(faceToken: String, faceSetToken: String) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("api_key", apiKey)
            .addFormDataPart("api_secret", apiSecret)
            .addFormDataPart("faceset_token", faceSetToken)
            .addFormDataPart("face_tokens", faceToken)
            .build()



        val request = Request.Builder().url(faceSetAddFaceApiUrl).post(requestBody).build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                println("ERRO ao adicionar face ao FaceSet: $errorBody")
                throw IOException("Falha ao adicionar rosto ao FaceSet: $errorBody")
            }

            println("DEBUG - Rosto $faceToken adicionado ao FaceSet $faceSetToken com sucesso.")
        }
    }
    /**
     * NOVO (Principal): Busca um rosto em um FaceSet.
     * @return O 'face_token' do rosto que deu match, ou nulo se não houver match.
     */
    fun searchFaceInFaceSet(faceSetToken: String, imageFile: MultipartFile): String? {
        try {
            val processedImageBytes = imageProcessingService.processImageForApi(imageFile)

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("api_key", apiKey)
                .addFormDataPart("api_secret", apiSecret)
                .addFormDataPart("faceset_token", faceSetToken)
                .addFormDataPart(
                    "image_file",
                    "search_image.jpg",
                    processedImageBytes.toRequestBody("image/jpeg".toMediaType())
                )
                .build()

            val request = Request.Builder().url(searchApiUrl).post(requestBody).build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    println("ERRO - Resposta da busca Face++ não foi bem-sucedida: ${response.body?.string()}")
                    return null
                }
                val responseBody = response.body!!.string()
                println("DEBUG - Resposta da busca Face++: $responseBody")

                val jsonObject = JSONObject(responseBody)

                val results = jsonObject.optJSONArray("results")
                if (results != null && results.length() > 0) {
                    val bestMatch = results.getJSONObject(0)
                    val confidence = bestMatch.getDouble("confidence")
                    if (confidence >= confidenceThreshold) {
                        return bestMatch.getString("face_token")
                    }
                }
                return null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}