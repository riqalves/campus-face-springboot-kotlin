package br.com.fatec.campusface.dto
import br.com.fatec.campusface.models.Role
import br.com.fatec.campusface.models.User
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
        fun fromEntity(id: String, user: User) = UserDTO(
            id = id,
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
    val email: String,
    val password: String
)

