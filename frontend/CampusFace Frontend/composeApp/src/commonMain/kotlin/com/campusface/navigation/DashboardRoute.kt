package com.campusface.navigation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface DashboardRoute {

    @Serializable
    @SerialName("membro")
    data object Membro : DashboardRoute

    @Serializable
    data class AdicionarMembro(val role: String = "MEMBER") : DashboardRoute

    @Serializable
    data class QrCodeMembro(val organizationId: String) : DashboardRoute

    @Serializable
    data class ChangeRequest(val organizationId: String) : DashboardRoute

    @Serializable
    @SerialName("qrcode-validador")
    data object QrCodeValidador : DashboardRoute

    @Serializable
    @SerialName("administrar")
    data object Administrar : DashboardRoute

    @Serializable
    data object CriarHub : DashboardRoute

    @Serializable
    @SerialName("validar")
    data object Validar : DashboardRoute

    @Serializable
    @SerialName("perfil")
    data object MeuPerfil : DashboardRoute

    @Serializable
    @SerialName("sair")
    data object Sair : DashboardRoute

    @Serializable
    @SerialName("detalhes-hub")
    data class DetalhesHub(val hubId: String) : DashboardRoute
}

object DashboardRouteNames {
    const val MEMBRO = "com.campusface.navigation.DashboardRoute.Membro"
    const val ADMINISTRAR = "com.campusface.navigation.DashboardRoute.Administrar"
    const val VALIDAR = "com.campusface.navigation.DashboardRoute.Validar"
    const val MEU_PERFIL = "com.campusface.navigation.DashboardRoute.MeuPerfil"
    const val SAIR = "com.campusface.navigation.DashboardRoute.Sair"
}