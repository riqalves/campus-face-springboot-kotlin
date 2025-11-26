package br.com.fatec.campusface.controller

import br.com.fatec.campusface.dto.ApiResponse
import br.com.fatec.campusface.dto.UserDTO
import br.com.fatec.campusface.models.User
import br.com.fatec.campusface.repository.OrganizationMemberRepository
import br.com.fatec.campusface.service.AuthCodeService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/face")
class FaceValidationController(
    private val authCodeService: AuthCodeService,
    private val orgMemberRepository: OrganizationMemberRepository
) {

    /**
     * Endpoint para um VALIDATOR identificar um MEMBER enviando uma foto.
     * O sistema busca o rosto na organização à qual o validador pertence.
     */
    @PostMapping("/identify", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("hasRole('VALIDATOR')")
    fun identifyFace(
        @RequestPart("image") imageFile: MultipartFile?,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<Any>> {
        return try {
            if (imageFile == null || imageFile.isEmpty) {
                return ResponseEntity.badRequest().body(ApiResponse(success = false, message = "O arquivo de imagem é obrigatório.", data = null))
            }

            val validatorUser = authentication.principal as User

            val validatorMembership = orgMemberRepository.findById(validatorUser.id)
                ?: return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse(success = false, message = "Validador não está associado a nenhuma organização.", data = null))

            val organizationId = validatorMembership.organizationId

            val identifiedUser: UserDTO? = authCodeService.identifyUserByFace(organizationId, imageFile)

            if (identifiedUser != null) {
                ResponseEntity.ok(
                    ApiResponse(
                        success = true,
                        message = "Usuário identificado com sucesso!",
                        data = identifiedUser
                    )
                )
            } else {
                ResponseEntity.ok(
                    ApiResponse(
                        success = false,
                        message = "Nenhum usuário correspondente encontrado na organização.",
                        data = null
                    )
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(success = false, message = "Ocorreu um erro interno durante a identificação: ${e.message}", data = null)
            )
        }
    }
}