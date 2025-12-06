package com.campusface.screens.membroScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Imports do seu projeto
import com.campusface.components.AdaptiveScreenContainer
import com.campusface.data.Repository.ChangeRequestRepository
import com.campusface.data.Repository.LocalAuthRepository
import com.campusface.utils.AppEventBus // Importante para atualizar as listas
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readBytes


data class ChangeRequestUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class ChangeRequestViewModel(
    private val repository: ChangeRequestRepository = ChangeRequestRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangeRequestUiState())
    val uiState = _uiState.asStateFlow()

    fun sendRequest(orgId: String, imageBytes: ByteArray?, token: String?) {
        if (token.isNullOrBlank()) {
            _uiState.update { it.copy(error = "Token inválido.") }
            return
        }

        if (imageBytes == null) {
            _uiState.update { it.copy(error = "Selecione uma foto para enviar.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        repository.createChangeRequest(
            organizationId = orgId,
            imageBytes = imageBytes,
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
        _uiState.value = ChangeRequestUiState()
    }
}


@Composable
fun ChangeRequestScreen(
    navController: NavHostController,
    organizationId: String, // ID da organização vindo da rota
    viewModel: ChangeRequestViewModel = viewModel { ChangeRequestViewModel() }
) {

    val authRepository = LocalAuthRepository.current
    val authState by authRepository.authState.collectAsState()


    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }


    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var selectedImagePreview by remember { mutableStateOf<Any?>(null) }


    val launcher = rememberFilePickerLauncher(

        type = FileKitType.File(
            extensions = listOf("jpg", "jpeg", "png")
        ),
        title = "Selecione sua nova foto"
    ) { file ->
        if (file != null) {
            selectedImagePreview = file
            scope.launch {
                imageBytes = file.readBytes()
            }
        }
    }


    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            snackbarHostState.showSnackbar("Solicitação enviada com sucesso!")
            navController.popBackStack() // Volta para a tela de membros
            viewModel.resetState()
        }
    }

    LaunchedEffect(uiState.error) {

        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            println(error)
        }
    }

    AdaptiveScreenContainer {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("Atualizar Foto do Rosto", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(Modifier.height(40.dp))


                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(180.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF0F0F0))
                        .border(
                            width = 2.dp,
                            color = if (imageBytes != null) Color(0xFF00A12B) else Color.Gray,
                            shape = CircleShape
                        )
                        .clickable { launcher.launch() }
                ) {
                    if (selectedImagePreview != null) {
                        AsyncImage(
                            model = selectedImagePreview,
                            contentDescription = "Nova foto",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("Toque para escolher", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    "Esta foto será enviada para aprovação dos administradores.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(Modifier.height(40.dp))


                Button(
                    onClick = {
                        viewModel.sendRequest(organizationId, imageBytes, authState.token)
                    },
                    enabled = !uiState.isLoading && imageBytes != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        disabledContainerColor = Color.Gray
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            "Enviar Solicitação",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}