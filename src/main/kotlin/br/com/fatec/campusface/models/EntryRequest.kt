package br.com.fatec.campusface.models

import java.time.Instant

data class EntryRequest(
    val id:String = "",
    val userId: String? ="",
    val organizationId: String ="",
    val status: String = "",
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)