package br.com.fatec.campusface.repository

import br.com.fatec.campusface.models.OrganizationMember
import com.google.cloud.firestore.Firestore
import org.springframework.stereotype.Repository

@Repository
class OrganizationMemberRepository(private val firestore: Firestore) {

    private val collection = firestore.collection("organizationMembers")

    // Cria um membro apenas após aprovação do EntryRequest
    fun save(member: OrganizationMember): OrganizationMember {
        if (member.userId!!.isEmpty() || member.organizationId.isEmpty()) {
            throw IllegalArgumentException("O ID do usuário e da organização não podem ser vazios")
        }

        val docRef = collection.document() // Firestore gera o ID automaticamente
        docRef.set(member).get()

        println("DEBUG - OrganizationMember criado: $member")
        println("DEBUG - ID gerado no Firestore: ${docRef.id}")

        return member.copy() // retorna o próprio objeto com os IDs corretos
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

    // Buscar membro específico
    fun findByUserAndOrganizationId(userId: String, organizationId: String): OrganizationMember? {
        val snapshot = collection
            .whereEqualTo("userId", userId)
            .whereEqualTo("organizationId", organizationId)
            .get()
            .get()
        return snapshot.documents.firstOrNull()?.toObject(OrganizationMember::class.java)
    }
}
