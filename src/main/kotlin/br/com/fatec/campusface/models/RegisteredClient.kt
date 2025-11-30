package br.com.fatec.campusface.models

import java.time.Instant

data class RegisteredClient(
    val id: String = "",
    val organizationId: String = "",
    val ipAddress: String = "",
    val port: String = "",
    val name: String = "", //Exemplo: Portaria Principal

    val lastCheckin: Instant = Instant.now(),
    val status: ClientStatus = ClientStatus.ONLINE,

    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)

enum class ClientStatus{
    ONLINE,
    OFFLINE,
    UNREACHABLE,
}
