package br.com.fatec.campusface.models

data class OrganizationMember(
    val organizationId: String = "",
    val userId: String? = "",
    val currentFaceId: String = ""
)