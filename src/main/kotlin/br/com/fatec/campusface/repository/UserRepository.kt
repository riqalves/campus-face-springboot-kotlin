package br.com.fatec.campusface.repository

import br.com.fatec.campusface.models.User
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FieldPath
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
        val documentSnapshot = collection.document(id).get().get()

        if (documentSnapshot.exists()) {
            val userObject = documentSnapshot.toObject(User::class.java)

            return userObject?.copy(id = documentSnapshot.id)
        }

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
        return document.toObject(User::class.java).copy(id = document.id)
    }


    fun delete(id: String): Boolean {
        return try {
            collection.document(id).delete().get()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Busca uma lista de usuários com base em uma lista de IDs.
     * @param userIds A lista de IDs dos usuários a serem buscados.
     * @return Uma lista de objetos User correspondentes aos IDs encontrados.
     */
    fun findAllByIds(userIds: List<String>): List<User> {
        // Se a lista de IDs estiver vazia, retorna uma lista vazia para evitar uma consulta desnecessária.
        if (userIds.isEmpty()) {
            return emptyList()
        }

        // Usa a consulta 'whereIn' com o ID do documento para buscar todos de uma vez.
        // O Firestore limita as consultas 'in' a um máximo de 30 itens por vez.
        // Para listas maiores, seria necessário dividir a busca em lotes (chunks).
        // Para este projeto, 30 é um limite razoável.
        val snapshot = collection
            .whereIn(FieldPath.documentId(), userIds)
            .get()
            .get()

        // Mapeia os documentos encontrados para objetos User, preenchendo o campo 'id'.
        return snapshot.documents.mapNotNull { document ->
            document.toObject(User::class.java)?.copy(id = document.id)
        }
    }
}
