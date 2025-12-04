
package com.campusface.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.campusface.navigation.DashboardRoute // Assumindo o import das suas rotas
import com.campusface.navigation.DashboardRouteNames

private val railItems = listOf(
    RailItem(
        label = "Membro",
        route = DashboardRouteNames.MEMBRO,
        icon = Icons.Filled.Home,
        destination = DashboardRoute.Membro
    ),
    RailItem(
        label = "Admin",
        route = DashboardRouteNames.ADMINISTRAR,
        icon = Icons.Filled.Settings,
        destination = DashboardRoute.Administrar
    ),
    RailItem(
        label = "Validar",
        route = DashboardRouteNames.VALIDAR,
        icon = Icons.Filled.Check,
        destination = DashboardRoute.Validar
    ),
    RailItem(
        label = "Perfil",
        route = DashboardRouteNames.MEU_PERFIL,
        icon = Icons.Filled.Person,
        destination = DashboardRoute.MeuPerfil
    ),
//    RailItem(
//        label = "Sair",
//        route = DashboardRouteNames.SAIR,
//        icon = Icons.Filled.ExitToApp,
//        destination = DashboardRoute.Sair
//    ),
)


@Composable
fun BottomBar(
    navController: NavHostController
) {
    val cores = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.background,
        indicatorColor = MaterialTheme.colorScheme.primary,

        unselectedIconColor = MaterialTheme.colorScheme.primary,
        unselectedTextColor = MaterialTheme.colorScheme.primary,
    )
    val backStackEntry by navController.currentBackStackEntryAsState()

    val currentRouteName = backStackEntry?.destination?.route

    NavigationBar(
        modifier = Modifier.padding(5.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {

        railItems.forEach { item ->


            val isSelected = currentRouteName == item.route

            NavigationBarItem(
                colors = cores,
                modifier = Modifier.padding(7.dp),
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
                label = { Text(item.label, color=MaterialTheme.colorScheme.primary, fontSize = 12.sp) },
            )
        }
        val isSelectedSair = currentRouteName == DashboardRouteNames.SAIR
        NavigationBarItem(
            colors = cores,
            modifier = Modifier.weight(1f),
            selected = isSelectedSair,
            onClick = {

                if (!isSelectedSair) {
                    navController.navigate(DashboardRoute.Sair) {
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
                    Icons.Filled.ExitToApp,
                    contentDescription = "Sair"
                )
            },
            label = { Text("Sair", fontSize = 12.sp) },
        )
    }
}