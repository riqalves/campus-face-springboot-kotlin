package br.com.fatec.campusface.dto

data class EntryRequestDTO(
    val id: String ="",
    val user: UserDTO?,
    val organizationId: String ="",
    val status: String = ""
)