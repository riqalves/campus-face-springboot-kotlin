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
import br.com.fatec.campusface.dto.GenerateCodeRequest
import br.com.fatec.campusface.dto.GeneratedCodeResponse
import br.com.fatec.campusface.dto.ValidateCodeRequest
import br.com.fatec.campusface.service.AuthCodeService

// Adicione esta anotação no nível da classe para proteger todos os endpoints dentro dela
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/validate")
class ValidationController(
    private val userService: UserService,
    private val facePlusPlusService: FacePlusPlusService,
    private val cloudinaryService: CloudinaryService,
    private val authCodeService: AuthCodeService
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


    /**
    * Endpoint para um MEMBRO solicitar a geração de um QR Code.
    */
    @PostMapping("/qr-code/generate")
    @PreAuthorize("hasRole('MEMBER')")
    fun generateQrCode(@RequestBody request: GenerateCodeRequest): ResponseEntity<ApiResponse<GeneratedCodeResponse>> {
        // TODO: Adicionar verificação de segurança para garantir que o usuário logado
        // só pode gerar códigos para seu próprio orgMemberId.
        return try {
            val authCode = authCodeService.generateCode(request.orgMemberId)
            val response = GeneratedCodeResponse(authCode.code, authCode.expirationTime)
            ResponseEntity.ok(ApiResponse(success = true, message = "Código gerado com sucesso.", data = response))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = e.message, data = null))
        }
    }

    /**
     * Endpoint para um VALIDATOR verificar um QR Code escaneado.
     */
    @PostMapping("/qr-code")
    @PreAuthorize("hasRole('VALIDATOR')")
    fun validateQrCode(@RequestBody request: ValidateCodeRequest): ResponseEntity<ApiResponse<Any>> {
        return try {
            val validatedMember = authCodeService.validateCode(request.code)
            ResponseEntity.ok(ApiResponse(success = true, message = "Validação bem-sucedida!", data = validatedMember))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse(success = false, message = e.message, data = null))
        }
    }
}