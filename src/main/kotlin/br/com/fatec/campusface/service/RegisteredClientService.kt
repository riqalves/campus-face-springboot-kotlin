package br.com.fatec.campusface.service

import br.com.fatec.campusface.dto.ClientCheckinDTO
import br.com.fatec.campusface.dto.RegisteredClientResponseDTO
import br.com.fatec.campusface.models.ClientStatus
import br.com.fatec.campusface.models.RegisteredClient
import br.com.fatec.campusface.models.Role
import br.com.fatec.campusface.repository.OrganizationMemberRepository
import br.com.fatec.campusface.repository.OrganizationRepository
import br.com.fatec.campusface.repository.RegisteredClientRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class RegisteredClientService(
    private val registeredClientRepository: RegisteredClientRepository,
    private val organizationRepository: OrganizationRepository,
    private val memberRepository: OrganizationMemberRepository,
    private val authService: AuthService // Para validar o token manual se vier no body
) {

    fun processCheckin(data: ClientCheckinDTO, tokenHeader: String?): RegisteredClientResponseDTO {
        // 1. Resolver o Token (Pode vir no Header ou no Body)
        val tokenRaw = tokenHeader?.replace("Bearer ", "") ?: data.token?.replace("Bearer ", "")
        ?: throw IllegalArgumentException("Token não fornecido.")

        val userId = authService.validateToken(tokenRaw)
        if (userId.isBlank()) {
            throw IllegalArgumentException("Token inválido ou expirado.")
        }

        // Validar o Hub
        // O Python manda "hub_id", mas no seu sistema isso é o "hubCode" ou o "ID"?
        // Assumindo que o Python manda o hubCode (ex: FATEC-ZL) para ser mais fácil de configurar lá.
        val organization = organizationRepository.findByHubCode(data.hub_id)
            ?: organizationRepository.findById(data.hub_id) // Tenta pelo ID se não achar pelo Code
            ?: throw IllegalArgumentException("Hub não encontrado: ${data.hub_id}")

        // validar Permissão (Quem está fazendo checkin é VALIDATOR desse Hub?)
        val member = memberRepository.findByUserIdAndOrganizationId(userId, organization.id)
        if (member == null || (member.role != Role.VALIDATOR && member.role != Role.ADMIN)) {
            throw IllegalAccessException("Este token não pertence a um Validador deste Hub.")
        }

        // Parse do IP e Porta (O Python manda "127.0.0.1:3000")
        val (ip, port) = if (data.server.contains(":")) {
            data.server.split(":")
        } else {
            listOf(data.server, "80") // Porta padrão se não vier
        }

        // Salvar ou Atualizar (Upsert do Cliente)
        // Verifica se já existe um cliente com esse IP nesse Hub
        val existingClient = registeredClientRepository.findByAddress(organization.id, ip, port)

        val clientToSave = if (existingClient != null) {
            existingClient.copy(
                lastCheckin = Instant.now(),
                status = ClientStatus.ONLINE,
                updatedAt = Instant.now()
            )
        } else {
            RegisteredClient(
                id = "", // Repo gera
                organizationId = organization.id,
                ipAddress = ip,
                port = port,
                name = "Totem Python (${member.id})",
                lastCheckin = Instant.now(),
                status = ClientStatus.ONLINE
            )
        }

        val savedClient = registeredClientRepository.save(clientToSave)

        return RegisteredClientResponseDTO(
            id = savedClient.id,
            hubCode = organization.hubCode,
            ipAddress = savedClient.ipAddress,
            port = savedClient.port,
            status = savedClient.status.name
        )
    }
}