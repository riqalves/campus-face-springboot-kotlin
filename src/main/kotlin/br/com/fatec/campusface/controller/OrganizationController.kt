package br.com.fatec.campusface.controller

import br.com.fatec.campusface.dto.ApiResponse
import br.com.fatec.campusface.models.OrganizationMember
import br.com.fatec.campusface.models.Organization
import br.com.fatec.campusface.service.OrganizationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/organizations")
class OrganizationController(
    private val organizationService: OrganizationService
) {

    // Criar uma nova organização
    @PostMapping
    fun createOrganization(@RequestBody organization: Organization): ResponseEntity<ApiResponse<Organization>> {
        return try {
            val savedOrganization = organizationService.createOrganization(organization)
            ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse(
                    success = true,
                    message = "Organização criada com sucesso",
                    data = savedOrganization
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    success = false,
                    message = "Erro ao criar organização: ${e.message}",
                    data = null
                )
            )
        }
    }

    // Listar todas as organizações
    @GetMapping
    fun getAllOrganizations(): ResponseEntity<ApiResponse<List<Organization>>> {
        return try {
            val organizations = organizationService.getAllOrganizations()
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Organizações listadas com sucesso",
                    data = organizations
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    success = false,
                    message = "Erro ao buscar organizações: ${e.message}",
                    data = null
                )
            )
        }
    }

    // Buscar organização por ID
    @GetMapping("/{id}")
    fun getOrganizationById(@PathVariable id: String): ResponseEntity<ApiResponse<Organization>> {
        return try {
            val organization = organizationService.getOrganizationById(id)
            if (organization != null) {
                ResponseEntity.ok(
                    ApiResponse(
                        success = true,
                        message = "Organização encontrada",
                        data = organization
                    )
                )
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse(
                        success = false,
                        message = "Organização não encontrada",
                        data = null
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    success = false,
                    message = "Erro ao buscar organização: ${e.message}",
                    data = null
                )
            )
        }
    }

    // Atualizar organização
    @PutMapping("/{id}")
    fun updateOrganization(
        @PathVariable id: String,
        @RequestBody organization: Organization
    ): ResponseEntity<ApiResponse<Organization>> {
        return try {
            val updated = organizationService.updateOrganization(id, organization)
            if (updated != null) {
                ResponseEntity.ok(
                    ApiResponse(
                        success = true,
                        message = "Organização atualizada com sucesso",
                        data = updated
                    )
                )
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse(
                        success = false,
                        message = "Organização não encontrada",
                        data = null
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    success = false,
                    message = "Erro ao atualizar organização: ${e.message}",
                    data = null
                )
            )
        }
    }

    // Deletar organização
    @DeleteMapping("/{id}")
    fun deleteOrganization(@PathVariable id: String): ResponseEntity<ApiResponse<Void>> {
        return try {
            val deleted = organizationService.deleteOrganization(id)
            if (deleted) {
                ResponseEntity.ok(
                    ApiResponse(
                        success = true,
                        message = "Organização deletada com sucesso",
                        data = null
                    )
                )
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse(
                        success = false,
                        message = "Organização não encontrada",
                        data = null
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    success = false,
                    message = "Erro ao deletar organização: ${e.message}",
                    data = null
                )
            )
        }
    }

    // Buscar apenas os membros de uma organização
    @GetMapping("/{id}/members")
    fun getMembersByOrganizationId(@PathVariable id: String): ResponseEntity<ApiResponse<List<OrganizationMember>>> {
        return try {
            val members = organizationService.getMembersByOrganizationId(id)
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Membros listados com sucesso",
                    data = members
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    success = false,
                    message = "Erro ao buscar membros: ${e.message}",
                    data = null
                )
            )
        }
    }
}
