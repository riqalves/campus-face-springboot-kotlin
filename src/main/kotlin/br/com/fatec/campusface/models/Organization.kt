package br.com.fatec.campusface.models


data class Organization(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val adminIds: List<String> = emptyList(),
    val validatorIds: List<String> = emptyList(),
    val memberIds: List<String> = emptyList(),
    val faceSetToken: String? = null
)