package com.campusface.data.Model



data class SolicitacaoEntrada(
    val id: String,
    val hubId: Int, // Usaremos Int para corresponder ao ID do Hub
    val solicitante: Usuario,
    val dataSolicitacao: String,
    val mensagem: String? = null
)