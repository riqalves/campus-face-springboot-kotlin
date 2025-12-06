package com.campusface.data.Model



data class Usuario(
    val id: String, // Identificador único do usuário (geralmente obrigatório)
    val nome: String,
    val cpf: String,
    val email: String,
    val fotoPerfilUrl: String
)
