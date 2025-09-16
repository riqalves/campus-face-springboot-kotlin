package br.com.fatec.campusface.models

data class OrganizationMember(
    val id: String = "",
    val organizationId: String = "",
    val userId: String? = "",
    val faceImageId: String? = ""
)