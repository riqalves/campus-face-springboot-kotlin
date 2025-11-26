package br.com.fatec.campusface.service

import br.com.fatec.campusface.dto.UserDTO
import br.com.fatec.campusface.models.User
import br.com.fatec.campusface.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val cloudinaryService: CloudinaryService,
    private val imageProcessingService: ImageProcessingService,
) {

    @Value("\${default.organization.id}")
    private lateinit var defaultOrganizationId: String

    fun createUser(userData: User, imageFile: MultipartFile?): UserDTO {
        // validacao de email
        if (userRepository.findByEmail(userData.email) != null) {
            throw IllegalArgumentException("Email já cadastrado.")
        }
        // Upload da imagem (se ouver)
        // o User generico pode ter foto, mas sera usada quando virar member em alguma org
        var imagePublicId: String? = null
        if (imageFile != null) {
            val processedImageBytes = imageProcessingService.processImageForApi(imageFile)
            val uploadResult = cloudinaryService.upload(processedImageBytes)
            imagePublicId = uploadResult["public_id"]
                ?: throw IllegalStateException("Public ID não foi retornado pelo Cloudinary.")
        }

        val userToSave = userData.copy(
            hashedPassword = passwordEncoder.encode(userData.hashedPassword),
            faceImageId = imagePublicId
        )
        val savedUser = userRepository.save(userToSave)

        return savedUser.toDTO()
    }


    private fun validateImage(imageFile: MultipartFile) {
        // Validação de tipo de arquivo
        val allowedTypes = listOf("image/png", "image/jpeg", "image/jpg")
        if (imageFile.contentType !in allowedTypes) {
            throw IllegalArgumentException("Formato de imagem inválido. Apenas PNG, JPG e JPEG são permitidos.")
        }

        // Validação de tamanho
        val maxSizeInBytes = 10 * 1024 * 1024 // 10 MB
        if (imageFile.size > maxSizeInBytes) {
            throw IllegalArgumentException("A imagem excede o tamanho máximo de 5MB.")
        }
    }

    private fun User.toDTO(): UserDTO {
        // Gera URL temporária apenas se tiver imagem
        val temporaryImageUrl = this.faceImageId?.let { publicId ->
            cloudinaryService.generateSignedUrl(publicId)
        }
        return UserDTO.fromEntity(this, temporaryImageUrl)
    }

    fun validateEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        return emailRegex.matches(email)
    }

    fun listUsers(): List<UserDTO> =
        userRepository.findAll().map { user -> user.toDTO() }

    fun getUserByEmail(email: String): UserDTO? {
        return userRepository.findByEmail(email)?.toDTO()
    }

    fun getUserById(id: String): UserDTO? {
        return userRepository.findById(id)?.toDTO()
    }
    fun deleteUser(id: String): Boolean = userRepository.delete(id)

    fun checkPassword(rawPassword: String, encodedPassword: String): Boolean =
        passwordEncoder.matches(rawPassword, encodedPassword)
}
