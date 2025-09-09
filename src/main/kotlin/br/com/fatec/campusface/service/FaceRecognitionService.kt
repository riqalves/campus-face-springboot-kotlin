package br.com.fatec.campusface.service

import com.azure.ai.vision.face.FaceClient
import com.azure.ai.vision.face.models.FaceDetectionModel
import com.azure.ai.vision.face.models.FaceRecognitionModel
import com.azure.core.util.BinaryData
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class FaceRecognitionService(
    private val faceClient: FaceClient // Injeta o cliente do Azure
) {

    /**
     * Verifica se o rosto em uma nova imagem corresponde ao rosto de uma imagem de referência (URL).
     *
     * @param referenceImageUrl A URL da imagem já cadastrada (do Cloudinary).
     * @param newImageFile A nova imagem capturada para verificação.
     * @return true se os rostos forem da mesma pessoa, false caso contrário.
     */
//    fun facesMatch(referenceImageUrl: String, newImageFile: MultipartFile): Boolean {
//        try {
//            // Define os modelos a serem usados
//            val detectionModel = FaceDetectionModel.DETECTION_03
//            val recognitionModel = FaceRecognitionModel.RECOGNITION_04
//            val returnFaceId = true
//
//            // 1. Detecta o rosto na imagem de referência (da URL)
//            // A chamada foi ajustada para passar os parâmetros na ordem correta
//            val referenceFaces = faceClient.detect(referenceImageUrl, detectionModel, recognitionModel, returnFaceId)
//            val referenceFaceId = referenceFaces.firstOrNull()?.faceId ?: throw RuntimeException("Nenhum rosto detectado na imagem de referência.")
//
//            // 2. Detecta o rosto na nova imagem (do upload)
//            val newImageBytes = BinaryData.fromBytes(newImageFile.bytes)
//            // A chamada foi ajustada para passar os parâmetros na ordem correta
//            val newFaces = faceClient.detect(newImageBytes, detectionModel, recognitionModel, returnFaceId)
//            val newFaceId = newFaces.firstOrNull()?.faceId ?: throw RuntimeException("Nenhum rosto detectado na nova imagem.")
//
//            // 3. Compara os dois rostos
//            val verifyResult = faceClient.verifyFaceToFace(newFaceId, referenceFaceId)
//
//            println("DEBUG - Confiança da verificação: ${verifyResult.confidence}")
//
//            // 4. Retorna o resultado. 'isIdentical' é true se a confiança for alta.
//            return verifyResult.isIdentical
//
//        } catch (e: Exception) {
//            println("ERRO - Falha na verificação facial com Azure: ${e.message}")
//            e.printStackTrace() // Adicionado para ver o erro completo no console
//            // Em caso de erro (ex: nenhum rosto detectado), consideramos que não houve match.
//            return false
//        }

}