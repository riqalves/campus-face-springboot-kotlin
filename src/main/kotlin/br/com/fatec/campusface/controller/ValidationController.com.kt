package br.com.fatec.campusface.controller

import br.com.fatec.campusface.dto.ApiResponse
import br.com.fatec.campusface.service.AwsFaceRecognitionService // Importe o serviço AWS
// import br.com.fatec.campusface.service.FaceRecognitionService // Comente ou remova o Azure Face Service
import br.com.fatec.campusface.service.UserService
// ... (outros imports do Azure Face para o test-detect, se ainda for usá-lo)
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
    // private val faceRecognitionService: FaceRecognitionService, // Comente ou remova o Azure
    private val awsFaceRecognitionService: AwsFaceRecognitionService, // Injete o serviço AWS
//    private val faceClient: FaceClient // Mantenha para o /test-detect, se quiser
) {

    @PreAuthorize("hasRole('VALIDATOR')")
    @PostMapping("/face/{userId}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun validateFace(
        @PathVariable userId: String,
        @RequestPart("image") image: MultipartFile
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        return try {
            val userToValidate = userService.getUserById(userId)
                ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse(success = false, message = "Usuário a ser validado não encontrado.", data = null)
                )

            val referenceImageUrl = userToValidate.faceImageId

            val isMatch = awsFaceRecognitionService.facesMatch(referenceImageUrl, image)

            val responseData = mapOf("match" to isMatch)

            if (isMatch) {
                ResponseEntity.ok(
                    ApiResponse(
                        success = true,
                        message = "Validação facial bem-sucedida (AWS). Acesso permitido.",
                        data = responseData
                    )
                )
            } else {
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse(
                        success = false,
                        message = "Validação facial falhou (AWS). As faces não correspondem.",
                        data = responseData
                    )
                )
            }

        } catch (e: Exception) {
            println("ERRO na validação facial com AWS: ${e.message}")
            e.printStackTrace()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(success = false, message = "Ocorreu um erro interno durante a validação (AWS).", data = null)
            )
        }
    }

//    @PostMapping("/test-detect", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
//    fun testDetection(@RequestPart("image") image: MultipartFile): ResponseEntity<ApiResponse<Any>> {
//        // ... (seu método testDetection do Azure sem alterações)
//        try {
//            val imageBytes = BinaryData.fromBytes(image.bytes)
//            val detectedFaces = faceClient.detect(imageBytes, FaceDetectionModel.DETECTION_03, FaceRecognitionModel.RECOGNITION_04, false)
//
//            if (detectedFaces.isNotEmpty()) {
//                val firstFace = detectedFaces.first()
//                val faceRectangleMap = mapOf(
//                    "top" to firstFace.faceRectangle.top,
//                    "left" to firstFace.faceRectangle.left,
//                    "width" to firstFace.faceRectangle.width,
//                    "height" to firstFace.faceRectangle.height
//                )
//
//                val responseData: Map<String, Any> = mapOf(
//                    "facesDetected" to detectedFaces.size,
//                    "firstFaceRectangle" to faceRectangleMap
//                )
//                return ResponseEntity.ok(ApiResponse(success = true, message = "Detecção facial Azure funcionou!", data = responseData))
//            } else {
//                return ResponseEntity.ok(ApiResponse(success = true, message = "Nenhum rosto detectado na imagem Azure.", data = null))
//            }
//        } catch (e: Exception) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//                ApiResponse(success = false, message = "Erro durante a detecção Azure: ${e.message}", data = null)
//            )
//        }
//    }
}