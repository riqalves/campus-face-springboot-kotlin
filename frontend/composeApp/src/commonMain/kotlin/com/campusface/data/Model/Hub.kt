package com.campusface.data.Model

data class Hub(
    val id: Int,


    val nome: String,
    val status: String? = null,
    val quantidadeMembros: Int? = null,
    val temIconeGrupo: Boolean? = true
)
