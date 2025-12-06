package com.campusface.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import campusface.composeapp.generated.resources.Res
import campusface.composeapp.generated.resources.logo
import coil3.compose.AsyncImage
import com.campusface.components.AdaptiveScreenContainer
import com.campusface.data.Repository.LocalAuthRepository
import com.campusface.navigation.AppRoute
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@Composable
private fun inputColors(isError: Boolean = false) = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = if (isError) Color(0xFFEF4444) else Color(0xFFE5E7EB),
    focusedBorderColor = if (isError) Color(0xFFEF4444) else Color.Black,
    cursorColor = Color.Black,
    focusedLabelColor = if (isError) Color.Black else Color.Black,
    focusedTextColor = Color.Black,
    errorBorderColor = Color(0xFFEF4444)
)

@Composable
fun RegisterScreen(navController: NavHostController) {
    AdaptiveScreenContainer{
    val authRepo = LocalAuthRepository.current
    val authState by authRepo.authState.collectAsState()
    val scope = rememberCoroutineScope()
    val scroll = rememberScrollState()

    // Estados do Formulário
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var document by remember { mutableStateOf("") }

    // Estados da Imagem
    var imageBytes by remember { mutableStateOf<ByteArray>(ByteArray(0)) }
    var selectedFile by remember { mutableStateOf<PlatformFile?>(null) } // Para preview

    // Estados de Erro
    var fullNameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var documentError by remember { mutableStateOf(false) }
    var imageError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val launcher = rememberFilePickerLauncher(
        type = FileKitType.File(
            extensions = listOf("jpg", "jpeg", "png")
        ),
        title = "Selecione uma Imagem"
    ) { file: PlatformFile? ->
        if (file != null) {
            selectedFile = file
            imageError = false
            scope.launch {
                imageBytes = file.readBytes()
            }
        }
    }

    LaunchedEffect(authState) {
        if (authState.user != null && authState.error == null && !authState.isLoading) {
            navController.navigate(AppRoute.Login) {
                popUpTo(AppRoute.Register) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(420.dp)
                .verticalScroll(scroll)
                .padding(bottom = 32.dp)
        ) {
            Spacer(Modifier.height(32.dp))

            Image(
                painter = painterResource(Res.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(90.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "Crie sua conta",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Text(
                "Entre com sua conta do hub",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))


            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0F0F0))
                    .border(
                        width = 2.dp,
                        color = if (imageError) Color(0xFFEF4444) else Color(0xFFE0E0E0),
                        shape = CircleShape
                    )
                    .clickable { launcher.launch() }
            ) {
                if (selectedFile != null) {
                    // Mostra a imagem selecionada
                    AsyncImage(
                        model = selectedFile,
                        contentDescription = "Foto selecionada",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Placeholder se não tiver foto
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(60.dp)
                    )
                }


                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .padding(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            if (imageError) {
                Text("Foto obrigatória *", color = Color(0xFFEF4444), fontSize = 12.sp)
            } else {
                Text("Toque para adicionar foto", color = Color.Gray, fontSize = 12.sp)
            }

            Spacer(Modifier.height(32.dp))



            // Nome
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it; fullNameError = false },
                placeholder = { Text("Nome completo", color = Color(0xFFBDBDBD)) },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                isError = fullNameError,
                colors = inputColors(fullNameError)
            )
            if (fullNameError) ErrorText("Nome completo é obrigatório")

            Spacer(Modifier.height(16.dp))

            // Documento
            OutlinedTextField(
                value = document,
                onValueChange = { document = it; documentError = false },
                placeholder = { Text("CPF", color = Color(0xFFBDBDBD)) },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                isError = documentError,
                colors = inputColors(documentError)
            )
            if (documentError) ErrorText("CPF é obrigatório")

            Spacer(Modifier.height(16.dp))

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; emailError = false },
                placeholder = { Text("E-mail", color = Color(0xFFBDBDBD)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                isError = emailError,
                colors = inputColors(emailError)
            )
            if (emailError) ErrorText("E-mail válido é obrigatório")

            Spacer(Modifier.height(16.dp))

            // Senha
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; passwordError = false },
                placeholder = { Text("Senha", color = Color(0xFFBDBDBD)) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                isError = passwordError,
                colors = inputColors(passwordError)
            )
            if (passwordError) ErrorText("Senha é obrigatória")

            Spacer(Modifier.height(24.dp))


            if (authState.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = authState.error ?: "Erro desconhecido",
                        color = Color(0xFFEF4444),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
            } else if (errorMessage.isNotEmpty()) {

                Text(errorMessage, color = Color(0xFFEF4444), fontSize = 14.sp)
                Spacer(Modifier.height(12.dp))
            }


            Button(
                onClick = {
                    var hasError = false
                    errorMessage = ""

                    if (fullName.isBlank()) { fullNameError = true; hasError = true }
                    if (document.isBlank()) { documentError = true; hasError = true }
                    if (email.isBlank() || !email.contains("@")) { emailError = true; hasError = true }
                    if (password.isBlank()) { passwordError = true; hasError = true }

                    // Validação de Imagem Obrigatória
                    if (imageBytes == null || imageBytes!!.isEmpty()) {
                        imageError = true
                        hasError = true
                    }

                    if (hasError) {
                        errorMessage = "Verifique os campos acima"
                        return@Button
                    }

                    authRepo.register(fullName, email, password, document, imageBytes)
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = !authState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                if (authState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Criar conta", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(20.dp))

            TextButton(onClick = { navController.navigate(AppRoute.Login) }) {
                Text("Já tenho uma conta", color = Color.Gray, fontSize = 16.sp)
            }
        }
    }
    }
}

@Composable
fun ErrorText(text: String) {
    Text(
        text = text,
        color = Color(0xFFEF4444),
        fontSize = 12.sp,
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp)
    )
}