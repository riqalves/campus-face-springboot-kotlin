package br.com.fatec.campusface.models


data class OrganizationMember(
    val id: String = "",
    val organizationId: String = "",
    val userId: String? = "",
    val role: Role = Role.MEMBER,
    val status: MemberStatus = MemberStatus.ACTIVE,
)

enum class Role {
    MEMBER,
    VALIDATOR,
    ADMIN
}

enum class MemberStatus {
    PENDING,
    ACTIVE,
    INACTIVE
}