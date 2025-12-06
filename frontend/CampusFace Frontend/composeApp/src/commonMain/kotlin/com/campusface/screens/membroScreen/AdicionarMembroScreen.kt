package com.campusface.screens.membroScreen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Imports do seu projeto
import com.campusface.components.AdaptiveScreenContainer
import com.campusface.data.Repository.EntryRequestRepository
import com.campusface.data.Repository.LocalAuthRepository


data class AddMembroUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class AdicionarMembroViewModel(
    private val repository: EntryRequestRepository = EntryRequestRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMembroUiState())
    val uiState = _uiState.asStateFlow()


    fun solicitarEntrada(hubCode: String, token: String?, role: String) {
        if (token.isNullOrBlank()) {
            _uiState.update { it.copy(error = "Erro de autenticação.") }
            return
        }

        if (hubCode.isBlank()) {
            _uiState.update { it.copy(error = "Digite o código do hub.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        repository.entryRequestCreate(
            hubCode = hubCode,
            role = role,
            token = token,
            onSuccess = {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            },
            onError = { msg ->
                _uiState.update { it.copy(isLoading = false, error = msg) }
            }
        )
    }

    fun resetState() {
        _uiState.value = AddMembroUiState()
    }
}


@Composable
fun AdicionarMembroScreen(
    navController: NavHostController,

    targetRole: String = "MEMBER",
    viewModel: AdicionarMembroViewModel = viewModel { AdicionarMembroViewModel() }
) {
    val authRepository = LocalAuthRepository.current
    val authState by authRepository.authState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()


    var hubCode by remember { mutableStateOf("") }


    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()


    val screenTitle = if (targetRole == "VALIDATOR") "Tornar-se Validador" else "Entrar em um hub"


    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            snackbarHostState.showSnackbar("Solicitação enviada com sucesso!")
            navController.popBackStack()
            viewModel.resetState()
        }
    }


    LaunchedEffect(uiState.error) {
        uiState.error?.let { erro ->
            snackbarHostState.showSnackbar(erro)
        }
    }

    AdaptiveScreenContainer {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        Text(screenTitle, style = MaterialTheme.typography.titleMedium)
                    }
                }

                // form
                Column(
                    modifier = Modifier
                        .padding(top = 32.dp)
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        "Digite o código do hub",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    OutlinedTextField(
                        value = hubCode,
                        onValueChange = { hubCode = it },
                        label = { Text("Ex: FATEC-ZL-2025") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        singleLine = true
                    )

                    Button(
                        onClick = {

                            viewModel.solicitarEntrada(hubCode, authState.token, targetRole)
                        },
                        enabled = hubCode.isNotBlank() && !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text("Solicitar entrada")
                        }
                    }
                }
            }
        }
    }
}