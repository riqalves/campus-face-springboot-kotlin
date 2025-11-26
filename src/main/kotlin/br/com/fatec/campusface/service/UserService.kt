package br.com.fatec.campusface.service

import br.com.fatec.campusface.dto.UserDTO
import br.com.fatec.campusface.models.OrganizationMember
import br.com.fatec.campusface.models.Role
import br.com.fatec.campusface.models.User
import br.com.fatec.campusface.repository.OrganizationMemberRepository
import br.com.fatec.campusface.repository.OrganizationRepository
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
    private val organizationRepository: OrganizationRepository,
    private val organizationMemberRepository: OrganizationMemberRepository,
    private val facePlusPlusService: FacePlusPlusService
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

        // --- 3. Retorno do DTO (lógica existente) ---
        return savedUser.toDTO() // Usando uma função de extensão para converter
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
        val temporaryImageUrl = this.faceImageId?.let { publicId ->
            cloudinaryService.generateSignedUrl(publicId)
        }
        return UserDTO(
            id = this.id,
            fullName = this.fullName,
            email = this.email,
            role = this.role,
            document = this.document,
            faceImageId = this.faceImageId,
            faceToken = this.faceToken
        )
    }

    fun validateEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        return emailRegex.matches(email)
    }

    fun listUsers(): List<UserDTO> =
        userRepository.findAll().map { (id, user) -> UserDTO.fromEntity(id, user) }

    fun getUserByEmail(email: String): UserDTO? {
        val userModel: User? = userRepository.findByEmail(email)

        if (userModel != null) {
            return UserDTO(
                id = userModel.id,
                fullName = userModel.fullName,
                email = userModel.email,
                role = userModel.role,
                document = userModel.document,
                faceImageId = userModel.faceImageId ?: "",
                faceToken = userModel.faceToken,
            )
        }

        return null
    }

    fun getUserById(id: String): UserDTO? =
        userRepository.findById(id)?.let { user -> UserDTO.fromEntity(id, user) }

    fun deleteUser(id: String): Boolean = userRepository.delete(id)

    fun checkPassword(rawPassword: String, encodedPassword: String): Boolean =
        passwordEncoder.matches(rawPassword, encodedPassword)
}
