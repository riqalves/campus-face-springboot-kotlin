package br.com.fatec.campusface.models


data class Organization(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val admins: List<User> = emptyList(),
    val validators: List<User> = emptyList(),
    val members: List<OrganizationMember> = emptyList()
)
