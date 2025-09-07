package br.com.fatec.campusface.models

data class EntryRequest(
    val id:String = "",
    val userId: String? ="",
    val organizationId: String ="",
    val status: String = ""
)