package br.com.fatec.campusface.repository

import br.com.fatec.campusface.models.OrganizationMember
import com.google.cloud.firestore.Firestore
import org.springframework.stereotype.Repository

@Repository
class OrganizationMemberRepository(private val firestore: Firestore) {

    private val collection = firestore.collection("organizationMembers")

    fun save(member: OrganizationMember): OrganizationMember {
        if (member.userId!!.isEmpty() || member.organizationId.isEmpty()) {
            throw IllegalArgumentException("O ID do usuário e da organização não podem ser vazios")
        }

        val docRef = collection.document()
        val memberWithId = member.copy(id = docRef.id)

        docRef.set(memberWithId).get()

        println("DEBUG - OrganizationMember criado: $memberWithId")
        println("DEBUG - ID gerado no Firestore: ${docRef.id}")

        return memberWithId
    }

    fun findAllByUserId(userId: String): List<OrganizationMember> {
        val snapshot = collection
            .whereEqualTo("userId", userId)
            .get()
            .get()
        return snapshot.documents.mapNotNull { it.toObject(OrganizationMember::class.java) }
    }

    // Buscar todos os membros de uma organização
    fun findAllByOrganizationId(organizationId: String): List<OrganizationMember> {
        val snapshot = collection
            .whereEqualTo("organizationId", organizationId)
            .get()
            .get()
        return snapshot.documents.mapNotNull { it.toObject(OrganizationMember::class.java) }
    }

    fun findById(id: String): OrganizationMember? {
        val document = collection.document(id).get().get()
        return if (document.exists()) {
            document.toObject(OrganizationMember::class.java)?.copy(id = document.id)
        } else {
            null
        }
    }


    // Buscar membro específico
    fun findByUserAndOrganizationId(userId: String, organizationId: String): OrganizationMember? {
        val snapshot = collection
            .whereEqualTo("userId", userId)
            .whereEqualTo("organizationId", organizationId)
            .get()
            .get()
        return snapshot.documents.firstOrNull()?.toObject(OrganizationMember::class.java)
    }

    /**
     * Atualiza apenas o campo 'faceImageId' de um OrganizationMember.
     */
    fun updateFaceImageId(id: String, newFaceImagePublicId: String) {
        collection.document(id).update("faceImageId", newFaceImagePublicId).get()
    }
}
