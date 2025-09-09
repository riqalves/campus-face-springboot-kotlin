package br.com.fatec.campusface.service

import br.com.fatec.campusface.dto.UserDTO
import br.com.fatec.campusface.models.User
import br.com.fatec.campusface.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val cloudinaryService: CloudinaryService
) {

    fun createUser(userData: User, imageFile: MultipartFile): UserDTO {
        if (userRepository.findByEmail(userData.email) != null) {
            throw IllegalArgumentException("Email já cadastrado.")
        }

        validateImage(imageFile)

        val imageUrl = cloudinaryService.upload(imageFile)

        val userToSave = userData.copy(
            hashedPassword = passwordEncoder.encode(userData.hashedPassword),
            faceImageId = imageUrl
        )

        val savedUser = userRepository.save(userToSave)

        return UserDTO(
            id = savedUser.id,
            fullName = savedUser.fullName,
            email = savedUser.email,
            role = savedUser.role,
            document = savedUser.document,
            faceImageId = savedUser.faceImageId!!
        )
    }

    private fun validateImage(imageFile: MultipartFile) {
        // Validação de tipo de arquivo
        val allowedTypes = listOf("image/png", "image/jpeg", "image/jpg")
        if (imageFile.contentType !in allowedTypes) {
            throw IllegalArgumentException("Formato de imagem inválido. Apenas PNG, JPG e JPEG são permitidos.")
        }

        // Validação de tamanho (ex: máximo de 5MB)
        val maxSizeInBytes = 5 * 1024 * 1024 // 5 MB
        if (imageFile.size > maxSizeInBytes) {
            throw IllegalArgumentException("A imagem excede o tamanho máximo de 5MB.")
        }
    }

    fun validateEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        return emailRegex.matches(email)
    }

    fun listUsers(): List<UserDTO> =
        userRepository.findAll().map { (id, user) -> UserDTO.fromEntity(id, user) }

    fun getUserByEmail(email: String): UserDTO? {
        // 1. Busca o modelo 'User' do repositório. O resultado pode ser nulo.
        val userModel: User? = userRepository.findByEmail(email)

        // 2. Verifica se o usuário foi encontrado (se userModel não é nulo)
        if (userModel != null) {
            // 3. Se foi encontrado, cria e retorna um UserDTO com os dados do modelo.
            return UserDTO(
                id = userModel.id,
                fullName = userModel.fullName,
                email = userModel.email,
                role = userModel.role,
                document = userModel.document,
                faceImageId = userModel.faceImageId ?: "" // Usa um valor padrão se for nulo
            )
        }

        // 4. Se não foi encontrado, retorna nulo.
        return null
    }

    fun getUserById(id: String): UserDTO? =
        userRepository.findById(id)?.let { user -> UserDTO.fromEntity(id, user) }

    fun deleteUser(id: String): Boolean = userRepository.delete(id)

    fun checkPassword(rawPassword: String, encodedPassword: String): Boolean =
        passwordEncoder.matches(rawPassword, encodedPassword)
}
