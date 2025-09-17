package br.com.fatec.campusface.models


data class Organization(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val adminIds: List<String> = emptyList(),      // Alterado para List<String>
    val validatorIds: List<String> = emptyList(),  // Alterado para List<String>
    val memberIds: List<String> = emptyList()      // Alterado para List<String>
)