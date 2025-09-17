package br.com.fatec.campusface.service

import br.com.fatec.campusface.dto.OrganizationCreateDTO
import br.com.fatec.campusface.dto.OrganizationResponseDTO
import br.com.fatec.campusface.dto.OrganizationUpdateDTO
import br.com.fatec.campusface.dto.UserDTO
import br.com.fatec.campusface.models.Organization
import br.com.fatec.campusface.models.User
import br.com.fatec.campusface.repository.OrganizationRepository
import br.com.fatec.campusface.repository.UserRepository
import org.springframework.stereotype.Service


@Service
class OrganizationService(
    private val organizationRepository: OrganizationRepository,
    private val userRepository: UserRepository,
    private val cloudinaryService: CloudinaryService
) {

    /**
     * Cria uma nova organização.
     * @param orgData Os dados da nova organização (nome e descrição).
     * @param creatorAdmin O usuário ADMIN que está criando a organização.
     * @return A organização completa que foi salva.
     */
    fun createOrganization(orgData: OrganizationCreateDTO, creatorAdmin: User): Organization {
        // Validação básica para garantir que o nome não está vazio
        if (orgData.name.isBlank()) {
            throw IllegalArgumentException("O nome da organização não pode ser vazio.")
        }

        // Cria a nova organização, adicionando o ID do criador à lista de admins
        val newOrganization = Organization(
            name = orgData.name,
            description = orgData.description,
            adminIds = listOf(creatorAdmin.id) // Adiciona o criador como o primeiro admin
        )
        return organizationRepository.save(newOrganization)
    }

    fun deleteOrganization(id: String): Boolean {
        // A lógica de deleção não precisa mudar
        return organizationRepository.delete(id)
    }

    fun updateOrganization(id: String, orgData: OrganizationUpdateDTO): OrganizationResponseDTO? {
        val existingOrg = organizationRepository.findById(id) ?: return null

        val updatedOrg = existingOrg.copy(
            name = orgData.name,
            description = orgData.description
        )

        val savedOrg = organizationRepository.update(id, updatedOrg)

        return savedOrg?.let { org -> hydrateOrganization(org) }
    }


    fun getAllOrganizations(): List<OrganizationResponseDTO> {
        val organizations = organizationRepository.findAll()
        // Mapeia cada organização para seu DTO "hidratado"
        return organizations.map { org -> hydrateOrganization(org) }
    }

    fun getOrganizationById(id: String): OrganizationResponseDTO? {
        // Busca a organização com os IDs
        val organization = organizationRepository.findById(id)
        // Se encontrar, "hidrata" e retorna o DTO; senão, retorna nulo
        return organization?.let { org -> hydrateOrganization(org) }
    }

    /**
     * Método auxiliar privado que faz a mágica de "hidratar" os dados.
     * Converte um objeto Organization (com IDs) em um OrganizationResponseDTO (com UserDTOs completos).
     */
    private fun hydrateOrganization(organization: Organization): OrganizationResponseDTO {
        // Busca os usuários completos a partir das listas de IDs
        val admins = userRepository.findAllByIds(organization.adminIds).map { it.toDTO() }
        val validators = userRepository.findAllByIds(organization.validatorIds).map { it.toDTO() }
        val members = userRepository.findAllByIds(organization.memberIds).map { it.toDTO() }

        return OrganizationResponseDTO(
            id = organization.id,
            name = organization.name,
            description = organization.description,
            admins = admins,
            validators = validators,
            members = members
        )
    }

    /**
     * Função de extensão para converter um User (modelo) em um UserDTO (objeto de API).
     * Ela também gera a URL assinada da imagem.
     */
    private fun User.toDTO(): UserDTO {
        val temporaryImageUrl = this.faceImageId?.let { publicId ->
            cloudinaryService.generateSignedUrl(publicId)
        }
        return UserDTO(
            id = this.id,
            fullName = this.fullName,
            email = this.email,
            role = this.role,
            document = this.document,
            faceImageId = temporaryImageUrl
        )
    }


}