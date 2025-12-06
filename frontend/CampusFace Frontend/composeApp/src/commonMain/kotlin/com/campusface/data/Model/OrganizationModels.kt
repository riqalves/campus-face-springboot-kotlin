package com.campusface.data.Model

import kotlinx.serialization.Serializable


@Serializable
data class OrganizationCreateRequest(
    val name: String,
    val description: String,
    val hubCode: String
)


@Serializable
data class Organization(
    val id: String,
    val name: String,
    val description: String,
    val hubCode: String,

    val admins: List<User> = emptyList(),
    val validators: List<User> = emptyList(),
    val members: List<User> = emptyList()
)


@Serializable
data class OrganizationResponse(
    val success: Boolean,
    val message: String,
    val data: Organization? = null
)