package br.com.fatec.campusface.repository

import br.com.fatec.campusface.models.ChangeRequest
import br.com.fatec.campusface.models.RequestStatus
import com.google.cloud.firestore.Firestore
import org.springframework.stereotype.Repository

@Repository
class ChangeRequestRepository(private val firestore: Firestore) {
    private val collection = firestore.collection("changeRequests")

    fun save(request: ChangeRequest): ChangeRequest {
        val docRef = collection.document()
        val requestWithId = request.copy(id = docRef.id)
        docRef.set(requestWithId).get()
        return requestWithId
    }

    fun findById(id: String): ChangeRequest? {
        val doc = collection.document(id).get().get()
        return if (doc.exists()) doc.toObject(ChangeRequest::class.java) else null
    }

    // Para o Admin visualizar as solicitações pendentes
    fun findPendingByOrganizationId(orgId: String): List<ChangeRequest> {
        return collection
            .whereEqualTo("organizationId", orgId)
            .whereEqualTo("status", RequestStatus.PENDING.name)
            .get().get()
            .toObjects(ChangeRequest::class.java)
    }

    fun delete(id: String) {
        collection.document(id).delete().get()
    }
}