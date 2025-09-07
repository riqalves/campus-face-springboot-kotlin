package br.com.fatec.campusface.repository

import br.com.fatec.campusface.dto.UserDTO
import org.springframework.security.core.userdetails.User as SpringUser
import br.com.fatec.campusface.models.User
import com.google.cloud.firestore.Firestore
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Repository


@Repository
class UserRepository(private val firestore: Firestore) {

    private val collection = firestore.collection("users")

    fun save(user: User): User {
        // 1. Cria um novo documento com um ID gerado automaticamente pelo Firestore
        val docRef = collection.document()

        // 2. Salva o objeto 'user' no banco de dados.
        // O campo 'id' do objeto 'user' de entrada ainda está vazio neste ponto.
        docRef.set(user).get()

        println("ESTOU EM USERREPOSITORY: $user")
        println("DEBUG - ID gerado no Firestore: ${docRef.id}")

        // 3. Retorna uma CÓPIA do objeto 'user' original,
        //    mas agora com o campo 'id' preenchido com o valor gerado pelo Firestore.
        return user.copy(id = docRef.id)
    }

    fun findAll(): List<Pair<String, User>> {
        val snapshot = collection.get().get()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(User::class.java).let { user -> doc.id to user }
        }
    }

    fun findById(id: String): User? {
        // Busca a referência do documento pelo ID fornecido
        val documentSnapshot = collection.document(id).get().get()

        // Verifica se o documento realmente existe
        if (documentSnapshot.exists()) {
            // 1. Converte o corpo do documento para o objeto User.
            //    Neste ponto, o campo 'id' dentro do objeto ainda está vazio ("").
            val userObject = documentSnapshot.toObject(User::class.java)

            // 2. CRUCIAL: Retorna uma CÓPIA do objeto,
            //    mas agora com o campo 'id' preenchido com o ID real do documento.
            return userObject?.copy(id = documentSnapshot.id)
        }

        // Retorna nulo se o documento não for encontrado
        return null
    }
    fun findByEmail(email: String): User? {
        val snapshot = collection
            .whereEqualTo("email", email)
            .limit(1)
            .get()
            .get()

        if (snapshot.isEmpty) {
            return null
        }

        val document = snapshot.documents.first()
        // Retorna o seu modelo User, preenchendo o ID
        return document.toObject(User::class.java)?.copy(id = document.id)
    }


    fun findUserDetailByEmail(email: String): UserDetails {
        val userPair = findAll().firstOrNull { it.second.email == email }
            ?: throw UsernameNotFoundException("Usuário não encontrado")
        val user = userPair.second
        return SpringUser(
            user.email,
            user.hashedPassword,
            listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
        )
    }



    fun findUserDetailById(id: String): UserDetails? {
        println("FindUserDetailById debug: $id")
        val user = findById(id) ?: return null

        return SpringUser(
            user.email,
            user.hashedPassword,
            listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
        )
    }

    fun delete(id: String): Boolean {
        return try {
            collection.document(id).delete().get()
            true
        } catch (e: Exception) {
            false
        }
    }
}
