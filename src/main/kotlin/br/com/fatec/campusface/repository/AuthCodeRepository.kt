package br.com.fatec.campusface.repository

import br.com.fatec.campusface.models.AuthCode
import com.google.cloud.firestore.Firestore
import org.springframework.stereotype.Repository

@Repository
class AuthCodeRepository(private val firestore: Firestore) {
    private val collection = firestore.collection("authCodes")

    fun save(authCode: AuthCode): AuthCode {
        val docRef = collection.document()
        val codeWithId = authCode.copy(id = docRef.id)
        docRef.set(codeWithId).get()
        return codeWithId
    }

    fun findValidByCode(code: String): AuthCode? {
        return collection
            .whereEqualTo("code", code)
            .whereEqualTo("valid", true)
            .limit(1)
            .get().get()
            .toObjects(AuthCode::class.java).firstOrNull()
    }

    // Invalida um código após ele ser usado
    fun invalidateCode(id: String) {
        collection.document(id).update("valid", false).get()
    }

    // Invalida todos os códigos antigos de um membro antes de gerar um novo
    fun invalidatePreviousCodes(userId: String) {
        val batch = firestore.batch()
        // Alterado para buscar por "userId"
        val query = collection
            .whereEqualTo("userId", userId)
            .whereEqualTo("valid", true)
            .get().get()

        query.documents.forEach { doc ->
            batch.update(doc.reference, "valid", false)
        }
        batch.commit().get()
    }
}