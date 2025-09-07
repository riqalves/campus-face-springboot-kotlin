package br.com.fatec.campusface.service

import br.com.fatec.campusface.models.Organization
import br.com.fatec.campusface.models.OrganizationMember
import br.com.fatec.campusface.repository.OrganizationRepository
import org.springframework.stereotype.Service


@Service
class OrganizationService(
    private val organizationRepository: OrganizationRepository
) {

    fun createOrganization(organization: Organization): Organization {
        return organizationRepository.save(organization)
    }

    fun getAllOrganizations(): List<Organization> {
        return organizationRepository.findAll()
    }

    fun getOrganizationById(id: String): Organization? {
        return organizationRepository.findById(id)
    }

    fun updateOrganization(id: String, organization: Organization): Organization? {
        return organizationRepository.update(id, organization)
    }

    fun deleteOrganization(id: String): Boolean {
        return organizationRepository.delete(id)
    }

    fun getMembersByOrganizationId(id: String): List<OrganizationMember> {
        return organizationRepository.findMembersByOrganizationId(id)
    }
}