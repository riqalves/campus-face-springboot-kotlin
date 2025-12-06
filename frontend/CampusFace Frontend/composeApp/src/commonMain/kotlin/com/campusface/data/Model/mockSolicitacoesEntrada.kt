package com.campusface.data.Model


val usuarioAlice = Usuario(
    id = "u101",
    nome = "Alice Chaves",
    cpf = "111.111.111-11",
    email = "alice@mail.com",
    fotoPerfilUrl = "http://mockserver.com/foto_alice.jpg"
)

val usuarioBeto = Usuario(
    id = "u102",
    nome = "Beto Dias",
    cpf = "222.222.222-22",
    email = "beto@mail.com",
    fotoPerfilUrl = "http://mockserver.com/foto_beto.jpg"
)

val usuarioCecilia = Usuario(
    id = "u103",
    nome = "Cecília Torres",
    cpf = "333.333.333-33",
    email = "cecilia@mail.com",
    fotoPerfilUrl = "http://mockserver.com/foto_cecilia_v1.jpg"
)

val membros = listOf<Usuario>(usuarioCecilia, usuarioBeto, usuarioAlice )

val mockSolicitacoesEntrada = listOf(
    SolicitacaoEntrada(
        id = "se001",
        hubId = 1, // Fatec Zona Leste
        solicitante = usuarioAlice,
        dataSolicitacao = "2025-11-23T10:00:00Z",
        mensagem = "Gostaria de participar dos eventos de tecnologia do Hub!"
    ),
    SolicitacaoEntrada(
        id = "se002",
        hubId = 1, // Fatec Zona Leste
        solicitante = usuarioBeto,
        dataSolicitacao = "2025-11-23T15:30:00Z",
        mensagem = "Sou novo na Fatec e quero me conectar com outros alunos."
    ),

    SolicitacaoEntrada(
        id = "se003",
        hubId = 2,
        solicitante = usuarioCecilia,
        dataSolicitacao = "2025-11-24T09:00:00Z"
    )
)