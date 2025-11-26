package br.com.fatec.campusface.repository

import br.com.fatec.campusface.dto.EntryRequestDTO
import br.com.fatec.campusface.dto.EntryRequestResponseDTO
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
    fun save(entryRequestResponseDTO: EntryRequestResponseDTO): EntryRequestDTO {
        val docRef = collection.document() // Firestore gera o ID automático

        val user = userService.getUserById(entryRequestResponseDTO.userId!!)
        val dto = EntryRequestDTO(
            id = docRef.id,
            user = user,
            organizationId = entryRequestResponseDTO.organizationId,
            status = entryRequestResponseDTO.status
        )
        val userToSave = EntryRequestResponseDTO(
            id = docRef.id,
            userId = entryRequestResponseDTO.userId,
            organizationId = entryRequestResponseDTO.organizationId,
            status = entryRequestResponseDTO.status
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
            val request = docSnapshot.toObject(EntryRequestResponseDTO::class.java) ?: return null
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
            val request = doc.toObject(EntryRequestResponseDTO::class.java) ?: return@mapNotNull null
            val user = request.userId?.let { userService.getUserById(it) }

            EntryRequestDTO(
                id = doc.id,
                user = user,
                organizationId = request.organizationId,
                status = request.status
            )
        }
    }

    fun updateStatus(id: String, newStatus: String) {
        // A única responsabilidade deste método é encontrar o documento pelo ID e atualizar seu status.
        collection.document(id).update("status", newStatus).get()
    }




    // Buscar todos os pedidos de uma organização
    fun findByOrganizationId(organizationId: String): List<EntryRequestDTO> {
        val querySnapshot = collection
            .whereEqualTo("organizationId", organizationId)
            .get()
            .get()

        return querySnapshot.documents.mapNotNull { doc ->
            val request = doc.toObject(EntryRequestResponseDTO::class.java) ?: return@mapNotNull null
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
        val request = doc.toObject(EntryRequestResponseDTO::class.java) ?: return null
        val user = request.userId?.let { userService.getUserById(it) }

        return EntryRequestDTO(
            id = doc.id,
            user = user,
            organizationId = request.organizationId,
            status = request.status
        )
    }
}
