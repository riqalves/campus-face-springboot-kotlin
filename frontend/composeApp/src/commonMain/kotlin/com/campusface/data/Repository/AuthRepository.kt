package com.campusface.data.Repository

import androidx.compose.runtime.staticCompositionLocalOf
import com.campusface.data.BASE_URL
import com.campusface.data.Model.User
import com.campusface.data.Model.ApiResponse  // 👈 IMPORTA do arquivo separado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Ktor imports
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable


@Serializable
data class LoginData(
    val user: User,
    val token: String
)

data class AuthState(
    val isAuthenticated: Boolean = false,
    val user: User? = null,
    val token: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val role: String? = null
)


class AuthRepository {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        install(io.ktor.client.plugins.logging.Logging) {
            level = io.ktor.client.plugins.logging.LogLevel.ALL
        }
    }

    private val scope = CoroutineScope(Dispatchers.Main)

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // LOGIN
    fun login(email: String, password: String) {
        scope.launch {
            try {
                _authState.value = _authState.value.copy(
                    isLoading = true,
                    error = null
                )

                val response: ApiResponse<LoginData> = client.post(BASE_URL + "/auth/login") {
                    header("ngrok-skip-browser-warning", "true")
                    contentType(ContentType.Application.Json)
                    setBody(
                        mapOf(
                            "email" to email,
                            "password" to password
                        )
                    )
                }.body()

                if (response.success && response.data != null) {
                    _authState.value = AuthState(
                        isAuthenticated = true,
                        user = response.data.user,
                        token = response.data.token,
                        isLoading = false,
                        error = null,
                        role = response.data.user.document
                    )
                } else {
                    throw Exception(response.message)
                }

            } catch (e: Exception) {
                _authState.value = AuthState(
                    isAuthenticated = false,
                    user = null,
                    token = null,
                    isLoading = false,
                    error = "Falha no login: ${e.message}",
                )
            }
        }
    }

    // REGISTER
    fun register(
        fullName: String,
        email: String,
        password: String,
        document: String,
        imageBytes: ByteArray
    ) {
        scope.launch {
            try {
                _authState.value = _authState.value.copy(
                    isLoading = true,
                    error = null
                )

                val response: ApiResponse<User> = client.submitFormWithBinaryData(
                    url = "$BASE_URL/auth/register",
                    formData = formData {
                        append("fullName", fullName)
                        append("email", email)
                        append("hashedPassword", password)
                        append("document", document)

                        append("image", imageBytes, Headers.build {
                            append(HttpHeaders.ContentType, "image/jpeg")
                            append(HttpHeaders.ContentDisposition, "filename=\"face.jpg\"")
                        })
                    }
                ) {
                    header("ngrok-skip-browser-warning", "true")
                }.body()

                if (response.success && response.data != null) {
                    _authState.value = AuthState(
                        isAuthenticated = false,
                        user = response.data,
                        token = null,
                        isLoading = false,
                        error = null
                    )
                } else {
                    throw Exception(response.message)
                }

            } catch (e: Exception) {
                _authState.value = AuthState(
                    isAuthenticated = false,
                    user = null,
                    token = null,
                    isLoading = false,
                    error = "Falha ao registrar: ${e.message}"
                )
            }
        }
    }

    fun logout() {
        _authState.value = AuthState()
    }
}

val LocalAuthRepository = staticCompositionLocalOf<AuthRepository> {
    error("AuthRepository não foi fornecido")
}