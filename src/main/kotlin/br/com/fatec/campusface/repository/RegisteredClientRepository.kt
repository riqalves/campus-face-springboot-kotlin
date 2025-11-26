package br.com.fatec.campusface.repository

import br.com.fatec.campusface.models.ClientStatus
import br.com.fatec.campusface.models.RegisteredClient
import com.google.cloud.firestore.Firestore
import org.springframework.stereotype.Repository

@Repository
class RegisteredClientRepository(private val firestore: Firestore) {
    private val collection = firestore.collection("registeredClients")
    fun save(client: RegisteredClient): RegisteredClient {
        val docRef = if(client.id.isNotEmpty()) collection.document(client.id) else collection.document()
        val clientWithId = client.copy(id = docRef.id)
        docRef.set(clientWithId).get()
        return clientWithId
    }
    fun findById(id: String): RegisteredClient? {
        val doc = collection.document(id).get().get()
        return if (doc.exists()) doc.toObject(RegisteredClient::class.java) else null
    }


    /**
     * Busca clientes (totens) de uma organização específica que estejam com status ONLINE.
     * Usado pelo SyncService para saber para quem enviar as novas faces.
     */
    fun findByOrganizaitonIdAndStatus(organizationId: String, status: ClientStatus): List<RegisteredClient> {
        val snapshot = collection
            .whereEqualTo("organizaiton.id", organizationId)
            .whereEqualTo("status", status.name)
            .get().get()
        return snapshot.documents.mapNotNull{it.toObject(RegisteredClient::class.java)}
    }

    fun findByAddress(organizaitonId: String, ipAddress:String, port: String): RegisteredClient? {
        val snapshot = collection
            .whereEqualTo("organizationId",organizaitonId)
            .whereEqualTo("ipAddress",ipAddress)
            .whereEqualTo("port",port)
            .limit(1).get().get()
        return snapshot.documents.firstOrNull()?.toObject(RegisteredClient::class.java)
    }

}
