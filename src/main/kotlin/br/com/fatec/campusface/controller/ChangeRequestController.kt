package br.com.fatec.campusface.controller

import br.com.fatec.campusface.dto.ApiResponse
import br.com.fatec.campusface.service.ChangeRequestService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.security.Principal

data class ReviewAction(val action: String) // "APPROVE" ou "DENY"

@RestController
@RequestMapping("/api/change-requests")
class ChangeRequestController(
    private val changeRequestService: ChangeRequestService
) {

    /**
     * Endpoint para um MEMBRO criar uma solicitação de mudança de foto.
     */
    @PostMapping("/create", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("hasRole('MEMBER')")
    fun createRequest(
        @RequestParam("orgMemberId") orgMemberId: String,
        @RequestPart("image") newImageFile: MultipartFile,
        principal: Principal // Para verificar se o usuário logado é o dono do orgMemberId
    ): ResponseEntity<ApiResponse<Any>> {
        // TODO: Adicionar lógica para verificar se `principal.name` (userId)
        // corresponde ao userId dentro do orgMemberId para segurança.

        return try {
            val request = changeRequestService.createChangeRequest(orgMemberId, newImageFile)
            ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse(success = true, message = "Solicitação de mudança enviada com sucesso.", data = request)
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse(success = false, message = e.message!!, data = null)
            )
        }
    }

    /**
     * Endpoint para um ADMIN aprovar ou negar uma solicitação.
     */
    @PostMapping("/{requestId}/review")
    @PreAuthorize("hasRole('ADMIN')")
    fun reviewRequest(
        @PathVariable requestId: String,
        @RequestBody reviewAction: ReviewAction
    ): ResponseEntity<ApiResponse<Any>> {
        return try {
            val approve = when (reviewAction.action.uppercase()) {
                "APPROVE" -> true
                "DENY" -> false
                else -> throw IllegalArgumentException("Ação inválida. Use 'APPROVE' ou 'DENY'.")
            }

            changeRequestService.reviewChangeRequest(requestId, approve)

            val message = if (approve) "Solicitação aprovada e imagem atualizada." else "Solicitação negada."
            ResponseEntity.ok(ApiResponse(success = true, message = message, data = null))

        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse(success = false, message = e.message!!, data = null)
            )
        }
    }

    // Você também pode criar um endpoint GET para o admin listar as solicitações pendentes.
}