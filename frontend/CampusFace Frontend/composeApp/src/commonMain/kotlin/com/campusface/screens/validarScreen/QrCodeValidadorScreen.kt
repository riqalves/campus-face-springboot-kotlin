package com.campusface.screens.validarScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Imports do seu projeto
import com.campusface.components.AdaptiveScreenContainer
import com.campusface.data.Repository.LocalAuthRepository
import com.campusface.data.Repository.ValidationRepository
import com.campusface.data.Repository.ValidationResponseData
import com.campusface.isCameraSupported // Sua função utilitária de KMP
import qrscanner.CameraLens
import qrscanner.OverlayShape
import qrscanner.QrScanner


private fun buildImageUrl(imageId: String?): String {
    if (imageId.isNullOrBlank()) return ""
    return if (imageId.startsWith("http")) imageId
    else "https://res.cloudinary.com/dt2117/image/upload/$imageId" // Ajuste seu cloud name
}

data class ValidatorUiState(
    val isValidating: Boolean = false,
    val validationResult: ValidationResponseData? = null,
    val error: String? = null
)

class QrCodeValidadorViewModel(
    private val repository: ValidationRepository = ValidationRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ValidatorUiState())
    val uiState = _uiState.asStateFlow()

    fun validateCode(code: String, token: String?) {
        if (token.isNullOrBlank()) return


        if (_uiState.value.isValidating) return

        _uiState.update { it.copy(isValidating = true, error = null, validationResult = null) }

        repository.validateQrCode(
            code = code,
            token = token,
            onSuccess = { result ->
                _uiState.update { it.copy(isValidating = false, validationResult = result) }


                viewModelScope.launch {
                    delay(3000)
                    resetState()
                }
            },
            onError = { msg ->
                _uiState.update { it.copy(isValidating = false, error = msg) }


                viewModelScope.launch {
                    delay(3000)
                    resetState()
                }
            }
        )
    }

    fun resetState() {
        _uiState.value = ValidatorUiState()
    }
}


@Composable
fun QrCodeValidadorScreen(
    navController: NavHostController,
    viewModel: QrCodeValidadorViewModel = viewModel { QrCodeValidadorViewModel() }
) {
    val authRepository = LocalAuthRepository.current
    val authState by authRepository.authState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()


    var flashlightOn by remember { mutableStateOf(false) }
    var openImagePicker by remember { mutableStateOf(false) }
    var lastScannedCode by remember { mutableStateOf("") }

    AdaptiveScreenContainer {
        Box(modifier = Modifier.fillMaxSize()) {

            // --- 1. SCANNER DE FUNDO ---
            if (isCameraSupported()) {
                QrScanner(
                    modifier = Modifier.fillMaxSize(),
                    flashlightOn = flashlightOn,
                    cameraLens = CameraLens.Back,
                    openImagePicker = openImagePicker,
                    onCompletion = { code ->
                        // Lógica para evitar validação repetida do mesmo código instantaneamente
                        if (!uiState.isValidating &&
                            uiState.validationResult == null &&
                            uiState.error == null &&
                            code != lastScannedCode
                        ) {
                            lastScannedCode = code
                            viewModel.validateCode(code, authState.token)

                        }
                    },
                    onFailure = { /* Ignora erros de leitura de frame */ },
                    overlayShape = OverlayShape.Square,
                    imagePickerHandler = { openImagePicker = it },

                    )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Câmera não disponível neste dispositivo.")
                }
            }

            //HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = Color.White)
                }
            }

            //UI RESULTADO

            // LOADING
            if (uiState.isValidating) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }

            // SUCESSO UI
            AnimatedVisibility(
                visible = uiState.validationResult?.valid == true,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                val member = uiState.validationResult?.member
                val user = member?.user

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)), // Verde claro
                    elevation = CardDefaults.cardElevation(8.dp),
                    modifier = Modifier.padding(32.dp).fillMaxWidth(),

                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,

                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF43A047), modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Acesso Permitido!", style = MaterialTheme.typography.titleLarge, color = Color(0xFF1B5E20))

                        if (user != null) {
                            Spacer(Modifier.height(16.dp))
                            AsyncImage(
                                model = buildImageUrl(user.faceImageId),
                                contentDescription = "Foto do usuário",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(user.fullName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(member.role, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // ERRO UI
            AnimatedVisibility(
                visible = uiState.error != null || uiState.validationResult?.valid == false,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                val msg = uiState.error ?: uiState.validationResult?.message ?: "Erro desconhecido"

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)), // Vermelho claro
                    elevation = CardDefaults.cardElevation(8.dp),
                    modifier = Modifier.padding(32.dp).fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Error, null, tint = Color(0xFFD32F2F), modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Acesso Negado", style = MaterialTheme.typography.titleLarge, color = Color(0xFFB71C1C))
                        Spacer(Modifier.height(8.dp))
                        Text(msg, style = MaterialTheme.typography.bodyMedium, color = Color.Black)
                    }
                }
            }
        }
    }
}