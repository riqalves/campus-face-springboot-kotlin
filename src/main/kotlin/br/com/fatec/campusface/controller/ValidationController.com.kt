package br.com.fatec.campusface.controller

import br.com.fatec.campusface.dto.ApiResponse
import br.com.fatec.campusface.service.FacePlusPlusService
import br.com.fatec.campusface.service.UserService
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import br.com.fatec.campusface.service.CloudinaryService

// Adicione esta anotação no nível da classe para proteger todos os endpoints dentro dela
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/validate")
class ValidationController(
    private val userService: UserService,
    private val facePlusPlusService: FacePlusPlusService,
    private val cloudinaryService: CloudinaryService
) {

    @PreAuthorize("hasRole('VALIDATOR')")
    @PostMapping("/face/{userId}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun validateFace(
        @PathVariable userId: String,
        @RequestPart("image") image: MultipartFile?
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        return try {

            if (image == null || image.isEmpty) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse(success = false, message = "O arquivo de imagem para validação não pode ser nulo ou vazio.", data = null)
                )
            }

            val userToValidate = userService.getUserById(userId)
                ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse(success = false, message = "Usuário a ser validado não encontrado.", data = null)
                )

            val publicId = userToValidate.faceImageId
                ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse(success = false, message = "Usuário não possui uma imagem de referência cadastrada.", data = null)
                )

            val referenceImageUrl = cloudinaryService.generateSignedUrl(publicId)

            val isMatch = facePlusPlusService.facesMatch(referenceImageUrl, image)

            val responseData = mapOf("match" to isMatch)

            if (isMatch) {
                ResponseEntity.ok(
                    ApiResponse(
                        success = true,
                        message = "Validação facial bem-sucedida. Acesso permitido.",
                        data = responseData
                    )
                )
            } else {
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse(
                        success = false,
                        message = "Validação facial falhou. As faces não correspondem.",
                        data = responseData
                    )
                )
            }

        } catch (e: Exception) {
            println("ERRO na validação facial: ${e.message}")
            e.printStackTrace()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(success = false, message = "Ocorreu um erro interno durante a validação.", data = null)
            )
        }
    }
}