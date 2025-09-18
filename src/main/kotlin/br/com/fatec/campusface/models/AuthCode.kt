package br.com.fatec.campusface.models

import java.time.Instant

data class AuthCode(
    val id: String = "",
    val code: String = "",
    val organizationMemberId: String = "",
    val expirationTime: Instant = Instant.now(),
    val isValid: Boolean = true
)