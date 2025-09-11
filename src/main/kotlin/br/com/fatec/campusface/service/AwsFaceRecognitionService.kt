package br.com.fatec.campusface.service

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.rekognition.RekognitionClient
import software.amazon.awssdk.services.rekognition.model.Image
import software.amazon.awssdk.services.rekognition.model.CompareFacesRequest
import software.amazon.awssdk.services.rekognition.model.ComparedFace
import software.amazon.awssdk.services.rekognition.model.S3Object
import software.amazon.awssdk.services.rekognition.model.FaceMatch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException


@Service
class AwsFaceRecognitionService(
    private val rekognitionClient: RekognitionClient
) {
    // Limiar de similaridade. Ajuste conforme necessário (0 a 100).
    // Um valor de 90 significa 90% de similaridade.
    private val similarityThreshold = 90.0f

    /**
     * Compara o rosto em uma nova imagem com o rosto de uma imagem de referência no Cloudinary.
     *
     * @param referenceImageUrl A URL da imagem já cadastrada no Cloudinary.
     * @param newImageFile A nova imagem capturada para verificação.
     * @return true se os rostos forem da mesma pessoa e acima do limiar de similaridade, false caso contrário.
     */
    fun facesMatch(referenceImageUrl: String, newImageFile: MultipartFile): Boolean {
        try {
            val sourceImage = Image.builder().s3Object(
                S3Object.builder().bucket("").name(referenceImageUrl).build()
            ).build()


            val sourceImageRekognition = Image.builder()
                .bytes(SdkBytes.fromByteArray(downloadImageFromUrl(referenceImageUrl)))
                .build()

            // Imagem alvo (a nova imagem do upload)
            val targetImageRekognition = Image.builder()
                .bytes(SdkBytes.fromByteArray(newImageFile.bytes))
                .build()

            val compareFacesRequest = CompareFacesRequest.builder()
                .sourceImage(sourceImageRekognition)
                .targetImage(targetImageRekognition)
                .similarityThreshold(similarityThreshold)
                .build()

            val compareFacesResult = rekognitionClient.compareFaces(compareFacesRequest)

            val faceMatches = compareFacesResult.faceMatches()
            if (faceMatches.isNotEmpty()) {
                val bestMatch = faceMatches.first()
                println("DEBUG - Similaridade AWS: ${bestMatch.similarity()}%, Limiar: $similarityThreshold%")
                return bestMatch.similarity() >= similarityThreshold
            }

            println("DEBUG - Nenhuma correspondência facial encontrada pela AWS.")
            return false

        } catch (e: Exception) {
            println("ERRO - Falha na verificação facial com AWS Rekognition: ${e.message}")
            e.printStackTrace()
            return false
        }
    }

    private fun downloadImageFromUrl(url: String): ByteArray {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Falha ao baixar imagem do Cloudinary: $response")
            }
            return response.body!!.bytes()
        }
    }
}