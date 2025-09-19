package br.com.fatec.campusface.repository

import br.com.fatec.campusface.models.Organization
import br.com.fatec.campusface.models.OrganizationMember
import com.google.cloud.firestore.FieldValue
import com.google.cloud.firestore.Firestore
import org.springframework.stereotype.Repository


@Repository
class OrganizationRepository(private val firestore: Firestore) {

    private val collection = firestore.collection("organizations")


    fun save(organization: Organization): Organization {
        // gera id automaticamente pelo Firestore
        val docRef = collection.document()

        // cria uma cópia da organização com o id atribuído
        val organizationWithId = organization.copy(id = docRef.id)

        // salva no Firestore com o id gerado
        docRef.set(organizationWithId).get()

        println("DEBUG - Organização salva: $organizationWithId")
        println("DEBUG - ID gerado no Firestore: ${docRef.id}")

        return organizationWithId
    }




    fun findById(id: String): Organization? {
        val doc = collection.document(id).get().get()
        return if (doc.exists()) doc.toObject(Organization::class.java) else null
    }

    fun findAll(): List<Organization> {
        val docs = collection.get().get().documents
        return docs.mapNotNull { it.toObject(Organization::class.java) }
    }

    fun update(id: String, organization: Organization): Organization? {
        val docRef = collection.document(id)
        // Verifica se o documento existe antes de tentar atualizar
        if (docRef.get().get().exists()) {
            docRef.set(organization).get() // 'set' com um ID existente substitui o documento
            return organization
        }
        return null
    }

    fun delete(id: String): Boolean {
        val docRef = collection.document(id)
        val doc = docRef.get().get()
        return if (doc.exists()) {
            docRef.delete().get()
            true
        } else {
            false
        }
    }

    fun updateFaceSetToken(organizationId: String, faceSetToken: String) {
        collection.document(organizationId).update("faceSetToken", faceSetToken).get()
    }

    fun addMemberToOrganization(organizationId: String, memberId: String) {
        collection.document(organizationId).update("memberIds", FieldValue.arrayUnion(memberId))
    }

    /**
     * NOVO: Adiciona o ID de um novo validador à lista 'validatorIds' de uma organização.
     */
    fun addValidatorToOrganization(organizationId: String, validatorId: String) {
        collection.document(organizationId).update("validatorIds", FieldValue.arrayUnion(validatorId))
    }

    /**
     * NOVO: Adiciona o ID de um novo admin à lista 'adminIds' de uma organização.
     */
    fun addAdminToOrganization(organizationId: String, adminId: String) {
        collection.document(organizationId).update("adminIds", FieldValue.arrayUnion(adminId))
    }
}
