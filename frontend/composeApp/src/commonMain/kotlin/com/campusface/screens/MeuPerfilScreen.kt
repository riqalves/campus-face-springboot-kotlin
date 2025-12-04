package com.campusface.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.campusface.components.AdaptiveScreenContainer
import com.campusface.data.Model.User
import com.campusface.data.Repository.LocalAuthRepository
import com.campusface.data.Repository.UserRepository
import com.campusface.screens.administrarScreen.buildImageUrl
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readBytes


data class PerfilUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isDeleted: Boolean = false,
    val user: User? = null,
    val error: String? = null
)

class MeuPerfilViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState = _uiState.asStateFlow()


    fun loadUserProfile(userId: String, token: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            userRepository.getUser(userId, token)
                .onSuccess { user ->
                    _uiState.update { it.copy(isLoading = false, user = user) }
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false, error = exception.message) }
                }
        }
    }


    fun saveChanges(
        token: String,
        newName: String,
        newEmail: String,
        newDoc: String,
        newImageBytes: ByteArray?
    ) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {

                if (newImageBytes != null) {
                    val resultImg = userRepository.updateProfileImage(newImageBytes, token)

                    if (resultImg.isFailure) throw resultImg.exceptionOrNull()!!


                    resultImg.onSuccess { u -> _uiState.update { it.copy(user = u) } }
                }


                val resultText = userRepository.updateUserData(newName, newEmail, newDoc, token)

                resultText.onSuccess { updatedUser ->
                    _uiState.update {
                        it.copy(isLoading = false, isSuccess = true, user = updatedUser)
                    }
                }.onFailure { throw it }

            } catch (e: Throwable) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Erro desconhecido") }
            }
        }
    }
    fun deleteAccount(userId: String, token: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            userRepository.deleteUser(userId, token)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isDeleted = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Erro ao excluir: ${e.message}") }
                }
        }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }
}


@Composable
fun MeuPerfilScreen(
    viewModel: MeuPerfilViewModel = viewModel { MeuPerfilViewModel() }
) {

    val authRepository = LocalAuthRepository.current
    val authState by authRepository.authState.collectAsState()


    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()


    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var document by remember { mutableStateOf("") }


    var newImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var selectedImagePreview by remember { mutableStateOf<Any?>(null) }


    var isDataLoaded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (authState.user != null && !authState.token.isNullOrBlank()) {
            viewModel.loadUserProfile(authState.user!!.id, authState.token!!)
        }
    }


    LaunchedEffect(uiState.user) {
        if (uiState.user != null && !isDataLoaded) {
            nome = uiState.user!!.fullName
            email = uiState.user!!.email
            document = uiState.user!!.document ?: ""
            isDataLoaded = true
        }
    }


    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            snackbarHostState.showSnackbar("Perfil atualizado com sucesso!")
            viewModel.resetSuccess()
            newImageBytes = null
            selectedImagePreview = null
        }
    }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            authRepository.logout()
        }
    }


    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }


    val launcher = rememberFilePickerLauncher(// Se o backend NÃO suporta WebP, use apenas isso:
        type = FileKitType.File(
            extensions = listOf("jpg", "jpeg", "png")
        ),) { file ->
        if (file != null) {
            selectedImagePreview = file
            scope.launch { newImageBytes = file.readBytes() }
        }
    }


    val hasTextChanges = uiState.user?.let {
        it.fullName != nome || it.email != email || (it.document ?: "") != document
    } ?: false
    val hasImageChanges = newImageBytes != null

    AdaptiveScreenContainer {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { padding ->

            if (uiState.isLoading && !isDataLoaded) {

                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 30.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(40.dp))


                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF0F0F0))
                            .border(2.dp, Color(0xFFE0E0E0), CircleShape)
                            .clickable { launcher.launch() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImagePreview != null) {
                            AsyncImage(
                                model = selectedImagePreview,
                                contentDescription = "Nova foto",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (!uiState.user?.faceImageId.isNullOrBlank()) {
                            AsyncImage(
                                model = buildImageUrl(uiState.user?.faceImageId),
                                contentDescription = "Foto atual",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Person, null, tint = Color.Gray, modifier = Modifier.size(60.dp))
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                .padding(6.dp)
                        ) {
                            Icon(Icons.Default.AddAPhoto, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Text("Toque na foto para alterar", fontSize = 12.sp, color = Color.Gray)

                    Spacer(Modifier.height(30.dp))



                    PerfilInput(
                        value = nome,
                        onValueChange = { nome = it },
                        placeholder = "Nome Completo"
                    )
                    Spacer(Modifier.height(15.dp))

                    PerfilInput(
                        value = document,
                        onValueChange = { document = it },
                        placeholder = "Documento"
                    )
                    Spacer(Modifier.height(15.dp))

                    PerfilInput(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "E-mail"
                    )

                    Spacer(Modifier.height(40.dp))


                    Button(
                        onClick = {
                            if (authState.user != null) {
                                viewModel.saveChanges(
                                    token = authState.token!!,
                                    newName = nome,
                                    newEmail = email,
                                    newDoc = document,
                                    newImageBytes = newImageBytes
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            disabledContainerColor = Color.Gray
                        ),

                        enabled = !uiState.isLoading && (hasTextChanges || hasImageChanges)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                "Salvar Alterações",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    TextButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Excluir minha conta", color = Color.Red, fontSize = 14.sp)
                    }
                }
            }
        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Warning, null, tint = Color.Red) },
            title = { Text("Excluir Conta?") },
            text = { Text("Tem certeza que deseja excluir sua conta permanentemente? Esta ação não pode ser desfeita.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        if (authState.user != null && authState.token != null) {
                            viewModel.deleteAccount(authState.user!!.id, authState.token!!)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Sim, excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            },
            containerColor = Color.White
        )
    }

}


@Composable
fun PerfilInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = ""
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF1F1F1)),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                color = Color(0xFF9E9E9E),
                modifier = Modifier.padding(start = 20.dp),
                fontSize = 16.sp
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = Color.Black
            )
        )
    }
}