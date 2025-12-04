package com.campusface.screens

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.campusface.components.BottomBar
import com.campusface.components.Sidebar
import com.campusface.navigation.DashboardRoute
import com.campusface.screens.membroScreen.MembroScreen
import com.campusface.screens.membroScreen.AdicionarMembroScreen
import com.campusface.screens.administrarScreen.AdministrarScreen
import com.campusface.screens.administrarScreen.DetalhesHubScreen
import com.campusface.screens.membroScreen.ChangeRequestScreen
import com.campusface.screens.membroScreen.QrCodeMembroScreen
import com.campusface.screens.validarScreen.ValidarScreen
import com.campusface.screens.validarScreen.QrCodeValidadorScreen

@Composable
fun DashboardLayout(
    navController: NavHostController
) {
    val backStackEntry by navController.currentBackStackEntryAsState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isMobile = maxWidth < 600.dp

        if (isMobile) {
            Scaffold(
                bottomBar = {
                    BottomBar(navController = navController)
                }
            ) { paddingValues ->
                DashboardContentNavHost(
                    navController = navController,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                Sidebar(navController = navController)

                DashboardContentNavHost(
                    navController = navController,
                    modifier = Modifier.fillMaxWidth().widthIn(650.dp)
                )
            }
        }
    }
}

@Composable
fun DashboardContentNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = DashboardRoute.Membro,
        modifier = modifier
    ) {
        composable<DashboardRoute.Membro> {
            MembroScreen(navController = navController)
        }

        composable<DashboardRoute.AdicionarMembro> { backStackEntry ->

            val rota = backStackEntry.toRoute<DashboardRoute.AdicionarMembro>()


            AdicionarMembroScreen(
                navController = navController,
                targetRole = rota.role
            )
        }

        composable<DashboardRoute.Administrar> {
            AdministrarScreen(navController = navController)
        }

        composable<DashboardRoute.CriarHub> {
            CriarHubScreen(navController = navController)
        }

        composable<DashboardRoute.Validar> {
            ValidarScreen(navController = navController)
        }

        composable<DashboardRoute.MeuPerfil> {
            MeuPerfilScreen()
        }

        composable<DashboardRoute.DetalhesHub> { backStackEntry ->
            val rota = backStackEntry.toRoute<DashboardRoute.DetalhesHub>()
            DetalhesHubScreen(
                hubId = rota.hubId,
                navController = navController
            )
        }

        composable<DashboardRoute.QrCodeMembro> { backStackEntry ->
            val rota = backStackEntry.toRoute<DashboardRoute.QrCodeMembro>()

            QrCodeMembroScreen(
                navController = navController,
                organizationId = rota.organizationId
            )
        }

        composable<DashboardRoute.ChangeRequest> { backStackEntry ->
            val rota = backStackEntry.toRoute<DashboardRoute.ChangeRequest>()

            ChangeRequestScreen(
                navController = navController,
                organizationId = rota.organizationId
            )
        }

        composable<DashboardRoute.QrCodeValidador> {
            QrCodeValidadorScreen(navController = navController)
        }



        composable<DashboardRoute.Sair> {
            val authRepository = com.campusface.data.Repository.LocalAuthRepository.current

            LaunchedEffect(Unit) {
                authRepository.logout()
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Saindo...")
                }
            }
        }
    }
}