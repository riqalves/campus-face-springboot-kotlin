    package com.campusface.components

    import androidx.compose.foundation.layout.*
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.getValue
    import androidx.compose.ui.Modifier
    import androidx.navigation.NavHostController
    import androidx.navigation.compose.currentBackStackEntryAsState
    import com.campusface.navigation.DashboardRoute
    import com.campusface.navigation.DashboardRouteNames
    import androidx.compose.material3.NavigationRail
    import androidx.compose.material3.NavigationRailItem
    import androidx.compose.material3.Icon
    import androidx.compose.material3.Text
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.*
    import androidx.compose.material3.MaterialTheme
    import androidx.compose.material3.NavigationRailItemDefaults
    import androidx.compose.runtime.collectAsState
    import androidx.compose.runtime.getValue
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.graphics.vector.ImageVector
    import androidx.compose.ui.unit.dp
    import com.campusface.data.Repository.LocalAuthRepository

    data class RailItem(
        val label: String,
        val route: String,
        val icon: ImageVector,
        val destination: DashboardRoute
    )

    private val railItems = listOf(
        RailItem(
            label = "Membro",
            route = DashboardRouteNames.MEMBRO,
            icon = Icons.Filled.Home, // Exemplo
            destination = DashboardRoute.Membro
        ),
        RailItem(
            label = "Administrar",
            route = DashboardRouteNames.ADMINISTRAR,
            icon = Icons.Filled.Settings, // Exemplo
            destination = DashboardRoute.Administrar
        ),
        RailItem(
            label = "Validar",
            route = DashboardRouteNames.VALIDAR,
            icon = Icons.Filled.Check, // Exemplo
            destination = DashboardRoute.Validar
        ),
        RailItem(
            label = "Meu Perfil",
            route = DashboardRouteNames.MEU_PERFIL,
            icon = Icons.Filled.Person, // Exemplo
            destination = DashboardRoute.MeuPerfil
        ),
    //    RailItem(
    //        label = "Sair",
    //        route = DashboardRouteNames.SAIR,
    //        icon = Icons.Filled.ExitToApp, // Exemplo
    //        destination = DashboardRoute.Sair
    //    ),
    )


    @Composable
    fun Sidebar(
        navController: NavHostController
    ) {
        val authRepository = LocalAuthRepository.current
        val authState by authRepository.authState.collectAsState()
        val cores = NavigationRailItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.background,
            indicatorColor = MaterialTheme.colorScheme.primary,

            unselectedIconColor = MaterialTheme.colorScheme.primary,
            unselectedTextColor = MaterialTheme.colorScheme.primary,
        )

        val backStackEntry by navController.currentBackStackEntryAsState()

        val currentRouteName = backStackEntry?.destination?.route

        NavigationRail(
            modifier = Modifier.fillMaxHeight().padding(20.dp),
            containerColor = Color.Transparent
        ) {
            Spacer(Modifier.weight(1f))

            railItems.forEach { item ->
                val isSelected = currentRouteName == item.route

                NavigationRailItem(
                    colors = cores,
                    modifier = Modifier.padding(10.dp),
                    selected = isSelected,
                    onClick = {

                        if (!isSelected) {
                            navController.navigate(item.destination) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        Icon(
                            item.icon,
                            contentDescription = item.label
                        )
                    },
                    label = { Text(item.label, color=MaterialTheme.colorScheme.primary) },
                )
            }
            val isSelectedSair = currentRouteName == DashboardRouteNames.SAIR
            NavigationRailItem(
                colors = cores,
                modifier = Modifier.weight(1f),
                selected = isSelectedSair,
                onClick = {
                    authRepository.logout()
                },
                icon = {
                    Icon(
                         Icons.Filled.ExitToApp,
                        contentDescription = "Sair"
                    )
                },
                label = { Text("Sair") },
            )

        }
    }
