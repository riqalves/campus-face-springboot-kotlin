package com.campusface.data.Model

val mockSolicitacoesAtualizacao = listOf(
    SolicitacaoAtualizacao(
        id = "sa001",
        hubId = 1, // Fatec Zona Leste
        solicitante = usuarioCecilia,
        dataSolicitacao = "2025-11-24T10:00:00Z",
        fotoAntigaUrl = usuarioCecilia.fotoPerfilUrl ?: "http://mockserver.com/foto_antiga_padrao.jpg",
        fotoNovaUrl = "http://mockserver.com/foto_cecilia_v2_aprovada.jpg"
    ),
    SolicitacaoAtualizacao(
        id = "sa002",
        hubId = 1, // Fatec Zona Leste
        solicitante = usuarioAlice,
        dataSolicitacao = "2025-11-24T11:15:00Z",
        fotoAntigaUrl = usuarioAlice.fotoPerfilUrl ?: "http://mockserver.com/foto_antiga_padrao.jpg",
        fotoNovaUrl = "http://mockserver.com/foto_alice_v2_foco.jpg"
    )
)