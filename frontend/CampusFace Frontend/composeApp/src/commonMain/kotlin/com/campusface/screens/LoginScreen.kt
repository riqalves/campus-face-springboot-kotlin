package com.campusface.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import campusface.composeapp.generated.resources.Res
import campusface.composeapp.generated.resources.logo
import com.campusface.data.Repository.LocalAuthRepository
import com.campusface.components.AdaptiveScreenContainer
import com.campusface.navigation.AppRoute
import org.jetbrains.compose.resources.painterResource

@Composable
fun LoginScreen(navController: NavHostController) {
    AdaptiveScreenContainer {

        val authRepository = LocalAuthRepository.current
        val authState by authRepository.authState.collectAsState()

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf("") }

        var emailError by remember { mutableStateOf(false) }
        var passwordError by remember { mutableStateOf(false) }

        val isLoading = authState.isLoading


        LaunchedEffect(authState.error) {
            if (authState.error != null) {
                errorMessage = authState.error!!
            }
        }

        fun validateAndLogin() {
            emailError = email.isBlank()
            passwordError = password.isBlank()

            if (emailError || passwordError) {
                errorMessage = "Preencha todos os campos."
                return
            }

            errorMessage = ""
            authRepository.login(email, password)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.widthIn(max = 420.dp)
            ) {

                Image(
                    painter = painterResource(Res.drawable.logo),
                    contentDescription = "Ícone",
                    modifier = Modifier.size(90.dp)
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Entre agora",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Entre com sua conta do hub",
                    fontSize = 15.sp,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(32.dp))

                if (errorMessage.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEE2E2)
                    ) {
                        Text(
                            text = errorMessage,
                            color = Color(0xFFDC2626),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = false
                    },
                    placeholder = { Text("Digite seu e-mail", color = Color(0xFFBDBDBD)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    isError = emailError,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        errorBorderColor = Color(0xFFDC2626),
                        focusedBorderColor = Color.Black,
                        cursorColor = Color.Black
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )

                Spacer(Modifier.height(14.dp))

                // Senha
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = false
                    },
                    placeholder = { Text("Digite sua senha", color = Color(0xFFBDBDBD)) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    isError = passwordError,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        errorBorderColor = Color(0xFFDC2626),
                        focusedBorderColor = Color.Black,
                        cursorColor = Color.Black
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )

                Spacer(Modifier.height(24.dp))

                // Botão Entrar
                Button(
                    onClick = { validateAndLogin() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        disabledContainerColor = Color(0xFF1A1A1A).copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = if (isLoading) "Entrando..." else "Entrar",
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Criar conta
                OutlinedButton(
                    onClick = { navController.navigate(AppRoute.Register) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Black
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp,
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE5E7EB))
                    )
                ) {
                    Text(
                        text = "Não tenho uma conta",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        }
    }
}
