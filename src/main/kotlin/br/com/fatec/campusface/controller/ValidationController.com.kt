package br.com.fatec.campusface.controller

import br.com.fatec.campusface.dto.ApiResponse
import br.com.fatec.campusface.service.FaceRecognitionService
import br.com.fatec.campusface.service.UserService
import com.azure.ai.vision.face.FaceClient
import com.azure.ai.vision.face.models.FaceDetectionModel
import com.azure.ai.vision.face.models.FaceRecognitionModel
import com.azure.core.util.BinaryData
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/validate")
class ValidationController(
    private val userService: UserService,
    private val faceRecognitionService: FaceRecognitionService,
    private val faceClient: FaceClient
) {

    /**
     * Endpoint para validar o rosto de um usuário.
     * Acessível apenas por usuários com a role VALIDATOR ou ADMIN.
     */
//    @PreAuthorize("hasRole('VALIDATOR')")
//    @PostMapping("/face/{userId}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
//    fun validateFace(
//        @PathVariable userId: String,
//        @RequestPart("image") image: MultipartFile
//    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
//        return try {
//            // 1. Busca o usuário que está sendo validado para obter a URL da imagem de referência.
//            val userToValidate = userService.getUserById(userId)
//                ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
//                    ApiResponse(success = false, message = "Usuário a ser validado não encontrado.", data = null)
//                )
//
//            val referenceImageUrl = userToValidate.faceImageId
//                ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
//                    ApiResponse(success = false, message = "Usuário não possui uma imagem de referência cadastrada.", data = null)
//                )
//
//            // 2. Chama o serviço de reconhecimento facial para comparar as imagens.
//            val isMatch = faceRecognitionService.facesMatch(referenceImageUrl, image)
//
//            // 3. Prepara a resposta com base no resultado.
//            val responseData = mapOf("match" to isMatch)
//
//            if (isMatch) {
//                ResponseEntity.ok(
//                    ApiResponse(
//                        success = true,
//                        message = "Validação facial bem-sucedida. Acesso permitido.",
//                        data = responseData
//                    )
//                )
//            } else {
//                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
//                    ApiResponse(
//                        success = false,
//                        message = "Validação facial falhou. As faces não correspondem.",
//                        data = responseData
//                    )
//                )
//            }
//
//        } catch (e: Exception) {
//            println("ERRO na validação facial: ${e.message}")
//            e.printStackTrace()
//            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//                ApiResponse(success = false, message = "Ocorreu um erro interno durante a validação.", data = null)
//            )
//        }
//    }
    // Em ValidationController.kt

    @PostMapping("/test-detect", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun testDetection(@RequestPart("image") image: MultipartFile): ResponseEntity<ApiResponse<Any>> {
        try {
            val imageBytes = BinaryData.fromBytes(image.bytes)
            // Apenas chama a função de detecção
            val detectedFaces = faceClient.detect(imageBytes, FaceDetectionModel.DETECTION_03, FaceRecognitionModel.RECOGNITION_04, false)

            if (detectedFaces.isNotEmpty()) {
                val responseData = mapOf(
                    "facesDetected" to detectedFaces.size,
                    "firstFaceRectangle" to detectedFaces.first().faceRectangle
                )
                return ResponseEntity.ok(ApiResponse(success = true, message = "Detecção facial funcionou!", data = responseData))
            } else {
                return ResponseEntity.ok(ApiResponse(success = true, message = "Nenhum rosto detectado na imagem.", data = null))
            }
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(success = false, message = "Erro durante a detecção: ${e.message}", data = null)
            )
        }
    }
}