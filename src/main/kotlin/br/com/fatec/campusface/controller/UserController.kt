package br.com.fatec.campusface.controller

import br.com.fatec.campusface.dto.ApiResponse
import br.com.fatec.campusface.dto.UserDTO
import br.com.fatec.campusface.models.User
import br.com.fatec.campusface.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/users")
@SecurityRequirement(name = "bearerAuth")
class UserController(private val userService: UserService) {

    @GetMapping("/{id}")
    @Operation(summary = "Busca dados do usuário", description = "O usuário só pode visualizar o seu próprio perfil.")
    fun getUser(@PathVariable id: String, authentication: Authentication): ResponseEntity<ApiResponse<UserDTO>> {
        val currentUser = authentication.principal as User
        println("DEBUG [getUser] $id, $currentUser")
        // Regra de Segurança: Apenas o próprio dono dos dados pode vê-los
        if (currentUser.id != id) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse(success = false, message = "Acesso negado aos dados de outro usuário.", data = null))
        }

        val userDto = userService.getUserById(id)
        return if (userDto != null) {
            ResponseEntity.ok(ApiResponse(success = true, message = "Usuário encontrado.", data = userDto))
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(success = false, message = "Usuário não encontrado.", data = null))
        }
    }

    /**
     * Endpoint para atualizar apenas a foto de perfil.
     * Isso é útil caso o usuário queira melhorar sua foto para o reconhecimento facial.
     */
    @PatchMapping(value = ["/{id}/image"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "Atualiza a foto de perfil", description = "Permite que o usuário troque sua foto facial.")
    fun updateProfileImage(
        @PathVariable id: String,
        @RequestParam("image") image: MultipartFile,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<UserDTO>> {
        val currentUser = authentication.principal as User

        if (currentUser.id != id) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse(success = false, message = "Você não pode alterar a foto de outro usuário.", data = null))
        }

        return try {
            val updatedUser = userService.updateProfileImage(id, image)

            // TODO: Se esse usuário já for membro de organizações, precisaríamos disparar ChangeRequests ou Syncs.
            // Por enquanto, apenas atualiza o perfil base.

            ResponseEntity.ok(
                ApiResponse(success = true, message = "Foto de perfil atualizada com sucesso!", data = updatedUser)
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = e.message, data = null))
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui a conta do usuário", description = "Ação irreversível. Remove o usuário e sua foto do sistema.")
    fun deleteUser(@PathVariable id: String, authentication: Authentication): ResponseEntity<ApiResponse<Void>> {
        val currentUser = authentication.principal as User

        if (currentUser.id != id) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse(success = false, message = "Você não pode excluir a conta de outro usuário.", data = null))
        }

        val deleted = userService.deleteUser(id)
        return if (deleted) {
            ResponseEntity.ok(ApiResponse(success = true, message = "Conta excluída com sucesso.", data = null))
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(success = false, message = "Usuário não encontrado.", data = null))
        }
    }

    @GetMapping("/allusers")
    fun getAllUsers(): ResponseEntity<ApiResponse<List<UserDTO>>> {
        val users = userService.listUsers()

        return ResponseEntity.ok().body(ApiResponse(success = true, message = "aqui esta os usuarios",data = users))
    }

}