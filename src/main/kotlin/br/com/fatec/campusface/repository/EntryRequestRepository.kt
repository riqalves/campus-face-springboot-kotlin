package br.com.fatec.campusface.repository

import br.com.fatec.campusface.dto.EntryRequestDTO
import br.com.fatec.campusface.models.EntryRequest
import br.com.fatec.campusface.service.UserService
import com.google.cloud.firestore.Firestore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class EntryRequestRepository(private val firestore: Firestore) {

    @Autowired
    private lateinit var userService: UserService

    private val collection = firestore.collection("entryRequests")


    // Cria um novo pedido de entrada
    fun save(entryRequest: EntryRequest): EntryRequestDTO {
        val docRef = collection.document() // Firestore gera o ID automático

        val user = userService.getUserById(entryRequest.userId!!)
        val dto = EntryRequestDTO(
            id = docRef.id,
            user = user,
            organizationId = entryRequest.organizationId,
            status = entryRequest.status
        )
        val userToSave = EntryRequest(
            id = docRef.id,
            userId = entryRequest.userId,
            organizationId = entryRequest.organizationId,
            status = entryRequest.status
        )

        docRef.set(userToSave).get()

        println("DEBUG - EntryRequest criado: $dto")
        return dto
    }

    // Buscar por ID
    fun findById(id: String): EntryRequestDTO? {
        val docSnapshot = collection.document(id).get().get()
            println("DEBUG ENTRYREQUEST REPOSITORY - User not found: $docSnapshot")

        return if (docSnapshot.exists()) {
            val request = docSnapshot.toObject(EntryRequest::class.java) ?: return null
            val user = userService.getUserById(request.userId!!) ?: return null
            EntryRequestDTO(
                id = docSnapshot.id,
                user = user,
                organizationId = request.organizationId,
                status = request.status
            )

        } else {
            null
        }
    }

    // Buscar todos os pedidos pendentes de uma organização
    fun findPendingByOrganization(organizationId: String): List<EntryRequestDTO> {
        val snapshot = collection
            .whereEqualTo("organizationId", organizationId)
            .whereEqualTo("status", "WAITING")
            .get()
            .get()
        return snapshot.documents.mapNotNull { doc ->
            val request = doc.toObject(EntryRequest::class.java) ?: return@mapNotNull null
            val user = request.userId?.let { userService.getUserById(it) }

            EntryRequestDTO(
                id = doc.id,
                user = user,
                organizationId = request.organizationId,
                status = request.status
            )
        }
    }

    fun updateStatus(entryRequest: EntryRequest): EntryRequestDTO {
        return try {

            println("DEBUG UPDATE ENTRYREQUEST REPOSITORY: $entryRequest")
            val user = entryRequest.userId.let { userService.getUserById(it!!) }
            val snapshot = collection
                .whereEqualTo("userId", entryRequest.userId)
                .whereEqualTo("organizationId", entryRequest.organizationId)
                .get()
                .get()

            val doc = snapshot.documents.firstOrNull()
                ?: throw IllegalStateException("Nenhum EntryRequest encontrado para userId=${entryRequest.userId}, organizationId=${entryRequest.organizationId}")

            val existingRequest = doc.toObject(EntryRequest::class.java).copy(id = doc.id)

            if (existingRequest.status == entryRequest.status) {
                throw IllegalStateException("Status já está igual: '${entryRequest.status}' (id=${doc.id})")
            }

            val updated = existingRequest.copy(status = entryRequest.status)
            doc.reference.set(updated).get()

            val dto = EntryRequestDTO(
                id = updated.id,
                user = user,
                organizationId = updated.organizationId,
                status = updated.status
            )

            println("DEBUG repository - EntryRequest atualizado: $dto")
            dto
        } catch (e: Exception) {
            println("ERRO repository - Falha ao atualizar EntryRequest: ${e.message}")
            throw e // relança para subir a exceção
        }
    }




    // Buscar todos os pedidos de uma organização
    fun findByOrganizationId(organizationId: String): List<EntryRequestDTO> {
        val querySnapshot = collection
            .whereEqualTo("organizationId", organizationId)
            .get()
            .get()

        return querySnapshot.documents.mapNotNull { doc ->
            val request = doc.toObject(EntryRequest::class.java) ?: return@mapNotNull null
            val user = request.userId?.let { userService.getUserById(it) }
            EntryRequestDTO(
                id = doc.id,
                user = user,
                organizationId = request.organizationId,
                status = request.status
            )
        }
    }

    // Buscar um pedido específico de usuário + organização
    fun findByUserAndOrganization(userId: String, organizationId: String): EntryRequestDTO? {
        val snapshot = collection
            .whereEqualTo("userId", userId)
            .whereEqualTo("organizationId", organizationId)
            .get()
            .get()

        val doc = snapshot.documents.firstOrNull() ?: return null
        val request = doc.toObject(EntryRequest::class.java) ?: return null
        val user = request.userId?.let { userService.getUserById(it) }

        return EntryRequestDTO(
            id = doc.id,
            user = user,
            organizationId = request.organizationId,
            status = request.status
        )
    }
}
