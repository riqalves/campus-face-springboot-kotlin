package br.com.fatec.campusface.dto
import br.com.fatec.campusface.models.Role
import br.com.fatec.campusface.models.User
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.time.Instant


data class UserDTO(
    val id: String,
    val fullName: String,
    val email: String,
    val document: String?,
    val faceImageId: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun fromEntity(user: User, signedFaceUrl: String?) = UserDTO(
            id = user.id,
            fullName = user.fullName,
            email = user.email,
            document = user.document,
            faceImageId = user.faceImageId ?: "default.png",
            createdAt = user.createdAt,
            updatedAt = user.updatedAt,
        )
    }
}



data class ApiResponse<T>(
    val message: String?,
    val success: Boolean,
    val data: T? = null
)



data class LoginDTO(
    @field:NotBlank(message = "O email é obrigatório")
    @field:Email(message = "Formato de email inválido")
    val email: String,

    @field:NotBlank(message = "A senha é obrigatória")
    val password: String
)

