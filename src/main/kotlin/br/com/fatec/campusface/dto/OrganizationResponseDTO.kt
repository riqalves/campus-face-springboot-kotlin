package br.com.fatec.campusface.dto

import br.com.fatec.campusface.models.User


data class OrganizationResponseDTO(
    val id: String,
    val name: String,
    val description: String,
    val admins: List<UserDTO>,
    val validators: List<UserDTO>,
    val members: List<UserDTO>
)