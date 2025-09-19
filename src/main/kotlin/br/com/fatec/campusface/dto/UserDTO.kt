package br.com.fatec.campusface.dto
import br.com.fatec.campusface.models.Role
import br.com.fatec.campusface.models.User


data class UserDTO(
    val id: String,
    val fullName: String,
    val email: String,
    val role: Role,
    val document: String?,
    val faceImageId: String?, // URL assinada/temporária
    val faceToken: String?   // <-- CAMPO ADICIONADO
) {
    companion object {
        fun fromEntity(id: String, user: User) = UserDTO(
            id = id,
            fullName = user.fullName,
            email = user.email,
            document = user.document,
            faceImageId = user.faceImageId ?: "default.png",
            role = user.role,
            faceToken = user.faceToken
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

