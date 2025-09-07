package br.com.fatec.campusface.service

import br.com.fatec.campusface.dto.UserDTO
import br.com.fatec.campusface.models.User
import br.com.fatec.campusface.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun createUser(userData: User, imageBase64: String?): UserDTO {
        // Lógica para criptografar a senha, etc.
        val encryptedPassword = passwordEncoder.encode(userData.hashedPassword)
        val userToSave = userData.copy(
            hashedPassword = encryptedPassword,
            faceImageId = imageBase64 // ou sua lógica de imagem
        )

        // 1. Chama o repositório, que salva e retorna o modelo User completo
        val savedUser: User = userRepository.save(userToSave)

        // 2. AQUI O SERVIÇO CONVERTE o modelo 'User' para um 'UserDTO' seguro
        //    O hashedPassword NUNCA sai da camada de serviço.
        return UserDTO(
            id = savedUser.id,
            fullName = savedUser.fullName,
            email = savedUser.email,
            role = savedUser.role,
            document = savedUser.document,
            faceImageId = savedUser.faceImageId ?: ""
        )
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

    fun getUser(id: String): UserDTO? =
        userRepository.findById(id)?.let { user -> UserDTO.fromEntity(id, user) }

    fun deleteUser(id: String): Boolean = userRepository.delete(id)

    fun checkPassword(rawPassword: String, encodedPassword: String): Boolean =
        passwordEncoder.matches(rawPassword, encodedPassword)
}
