package br.com.fatec.campusface.dto

import br.com.fatec.campusface.models.ClientStatus
import java.time.Instant

data class RegisteredClientResponseDTO(
    val id: String,
    val name: String,
    val ipAddress: String?,
    val port: String,
    val status: ClientStatus,
    val lastCheckin: Instant
)

data class ClientCheckinDTO(
    val hubCode: String,
    val ipAddress: String,
    val port: String,
    val status: ClientStatus,
    val lastCheckin: Instant
)