package br.com.fatec.campusface.controller

import br.com.fatec.campusface.dto.*
import br.com.fatec.campusface.models.Organization
import br.com.fatec.campusface.models.User
import br.com.fatec.campusface.service.OrganizationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.lang.IllegalArgumentException

@RestController
@RequestMapping("/organizations")
class OrganizationController(private val organizationService: OrganizationService) {

    /**
     * Endpoint para criar uma nova organização.
     * Apenas usuários com a role ADMIN podem acessá-lo.
     * O criador é automaticamente definido como o primeiro admin da organização.
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    fun createOrganization(
        @RequestBody orgData: OrganizationCreateDTO,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<Organization>> {
        return try {
            val creatorAdmin = authentication.principal as User
            val savedOrganization = organizationService.createOrganization(orgData, creatorAdmin)
            ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse(success = true, message = "Organização criada com sucesso!", data = savedOrganization)
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse(success = false, message = e.message, data = null)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(success = false, message = "Erro inesperado ao criar organização.", data = null)
            )
        }
    }

    /**
     * Endpoint para listar todas as organizações com os detalhes de seus membros.
     * Acessível por qualquer usuário autenticado.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    fun getAllOrganizations(): ResponseEntity<ApiResponse<List<OrganizationResponseDTO>>> {
        return try {
            val organizations = organizationService.getAllOrganizations()
            ResponseEntity.ok(
                ApiResponse(success = true, message = "Organizações listadas com sucesso.", data = organizations)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(success = false, message = "Erro ao listar organizações: ${e.message}", data = null)
            )
        }
    }

    /**
     * Endpoint para buscar uma organização específica pelo ID com detalhes dos membros.
     * Acessível por qualquer usuário autenticado.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    fun getOrganizationById(@PathVariable id: String): ResponseEntity<ApiResponse<OrganizationResponseDTO>> {
        return try {
            val organization = organizationService.getOrganizationById(id)
            if (organization != null) {
                ResponseEntity.ok(
                    ApiResponse(success = true, message = "Organização encontrada.", data = organization)
                )
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse(success = false, message = "Organização não encontrada.", data = null)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(success = false, message = "Erro ao buscar organização: ${e.message}", data = null)
            )
        }
    }

    /**
     * Endpoint para atualizar o nome e a descrição de uma organização.
     * Acessível apenas por usuários com a role ADMIN.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateOrganization(
        @PathVariable id: String,
        @RequestBody orgData: OrganizationUpdateDTO // Usa o DTO de atualização
    ): ResponseEntity<ApiResponse<OrganizationResponseDTO>> {
        return try {
            val updated = organizationService.updateOrganization(id, orgData)
            if (updated != null) {
                ResponseEntity.ok(
                    ApiResponse(success = true, message = "Organização atualizada com sucesso.", data = updated)
                )
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse(success = false, message = "Organização não encontrada para atualização.", data = null)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(success = false, message = "Erro ao atualizar organização: ${e.message}", data = null)
            )
        }
    }

    /**
     * Endpoint para deletar uma organização.
     * Acessível apenas por usuários com a role ADMIN.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteOrganization(@PathVariable id: String): ResponseEntity<ApiResponse<Void>> {
        return try {
            val deleted = organizationService.deleteOrganization(id)
            if (deleted) {
                ResponseEntity.ok(
                    ApiResponse(success = true, message = "Organização deletada com sucesso.", data = null)
                )
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse(success = false, message = "Organização não encontrada para deleção.", data = null)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(success = false, message = "Erro ao deletar organização: ${e.message}", data = null)
            )
        }
    }
}