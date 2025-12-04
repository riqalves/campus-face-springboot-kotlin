package com.campusface.navigation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
sealed interface AppRoute {


    @Serializable
    @SerialName("login")
    data object Login : AppRoute

    @Serializable
    @SerialName("register")
    data object Register : AppRoute

    @Serializable
    @SerialName("dashboard")
    data object DashboardGraph : AppRoute

    @Serializable
    @SerialName("")
    data object Splash : AppRoute


}