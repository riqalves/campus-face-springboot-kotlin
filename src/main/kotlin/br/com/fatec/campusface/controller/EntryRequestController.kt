package br.com.fatec.campusface.controller

import br.com.fatec.campusface.dto.ApiResponse
import br.com.fatec.campusface.dto.EntryRequestDTO
import br.com.fatec.campusface.models.EntryRequest
import br.com.fatec.campusface.dto.UserDTO
import org.springframework.security.access.prepost.PreAuthorize
import br.com.fatec.campusface.service.EntryRequestService
import br.com.fatec.campusface.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/entry-requests")
class EntryRequestController(
    private val entryRequestService: EntryRequestService,
    private val userService: UserService
) {

    @PostMapping("/create")
    fun createRequest(@RequestBody entryRequest: EntryRequest): ResponseEntity<ApiResponse<EntryRequestDTO>> {
        return try {
            println("DEBUG - EntryRequestController $entryRequest")
            val request = entryRequestService.createRequest(entryRequest)

            ResponseEntity.status(HttpStatus.CREATED)
                .body(
                    ApiResponse(
                        success = true,
                        message = "Pedido criado com sucesso",
                        data = request
                    )
                )
        } catch (e: Exception) {
            println("ERRO - Falha ao criar EntryRequest: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                    ApiResponse(
                        success = false,
                        message = "Erro ao criar pedido: ${e.message}",
                        data = null
                    )
                )
        }
    }


    @GetMapping("/organization/{organizationId}")
    fun getRequestsByOrganization(@PathVariable organizationId: String): ResponseEntity<ApiResponse<Any>> {
        return try {
            val requests = entryRequestService.getRequestsByOrganization(organizationId)
            ApiResponse("Pedidos encontrados", true, requests)
            ResponseEntity.status(HttpStatus.OK)
                .body(
                    ApiResponse(
                        success = true,
                        message = "Pedidos encontrados",
                        data = requests
                    )
                )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(
                    ApiResponse(
                        success = false,
                        message = "Erro ao buscar pedidos: ${e.message}",
                        data = ""
                    )
                )
        }
    }

    @GetMapping("/{requestId}")
    fun getRequestById(@PathVariable requestId: String): ResponseEntity<ApiResponse<Any>> {
        return try {
            val request = entryRequestService.getRequestById(requestId)
            if (request != null) {
                ResponseEntity.status(HttpStatus.OK)
                    .body(
                        ApiResponse(
                            success = true,
                            message = "Solicitação encontrada",
                            data = request
                        )
                    )
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(
                        ApiResponse(
                            success = false,
                            message = "Solicitação não encontrada",
                            data = ""
                        )
                    )
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(
                    ApiResponse(
                        success = false,
                        message = "Erro ao buscar pedido $e",
                        data = ""
                    )
                )
        }
    }

    @PostMapping("/{requestId}/approve")
    fun approveRequest(
        @PathVariable requestId: String,
        @RequestBody entryRequest: EntryRequest
    ): ResponseEntity<ApiResponse<Any>> {
        return try {
            println("DEBUG - EntryRequestController $entryRequest")
            val user = userService.getUserById(requestId)
            val entryRequestDTO = EntryRequestDTO(
                id = requestId,
                user = user,
                organizationId = entryRequest.organizationId,
                status = "APPROVED",
            )
            val entryRequestWithId = entryRequest.copy(id = requestId)
            println("DEBUG - EntryRequestController $entryRequestDTO")

            val member = entryRequestService.approveRequest(entryRequestWithId)
            println("DEBUG - EntryRequestController MEMBER: $member")

            return if (member == null) {

                ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse(
                        success = false,
                        message = "Erro ao aprovar solicitação ",
                        data = ""
                    )
                )
            } else {
                ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse(
                        success = true,
                        message = "Solicitação aprovada",
                        data = member
                    )
                )
            }
        } catch (e: Exception) {
            println("Error on approveRequest: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    message = "Ocorreu um erro ao registrar a solicitação ${e.message}",
                    success = false,
                    data = ""
                )
            )
        }
    }


    @PostMapping("/{requestId}/reject")
// @PreAuthorize("hasRole('ADMIN')")
    fun rejectRequest(@PathVariable requestId: String): ResponseEntity<ApiResponse<Void>> {
        return try {

            entryRequestService.rejectRequest(requestId)

            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Solicitação rejeitada com sucesso.",
                    data = null
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                    ApiResponse(
                        success = false,
                        message = "Erro ao rejeitar pedido: ${e.message}",
                        data = null
                    )
                )
        }
    }


    @GetMapping("/whoami")
    @PreAuthorize("isAuthenticated()")
    fun whoAmI(authentication: Authentication): ResponseEntity<ApiResponse<Any>> {
        try {

            println("TESTE whoAmI $authentication")

            val userModel = authentication.principal as br.com.fatec.campusface.models.User

            val userResponseDto = UserDTO(
                id = userModel.id,
                fullName = userModel.fullName,
                email = userModel.email,
                role = userModel.role,
                document = userModel.document,
                faceImageId = userModel.faceImageId!!,
                faceToken = userModel.faceToken
            )

            val authInfo = mapOf(
                "user" to userResponseDto, // Retornando o DTO formatado
                "authorities" to authentication.authorities.map { it.authority },
                "principalType" to authentication.principal.javaClass.name
            )

            return ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Dados do usuário autenticado.",
                    data = authInfo
                )
            )
        } catch (e: ClassCastException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    success = false,
                    message = "Erro de cast! O principal não é do tipo esperado. Tipo encontrado: ${authentication.principal.javaClass.name}. Erro: ${e.message}",
                    data = null
                )
            )
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    success = false,
                    message = "Erro ao recuperar dados de autenticação: ${e.message}",
                    data = null
                )
            )
        }
    }

}
