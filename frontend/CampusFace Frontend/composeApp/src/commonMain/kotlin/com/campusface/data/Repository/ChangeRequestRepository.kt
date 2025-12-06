package com.campusface.data.Repository

import com.campusface.data.BASE_URL
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
data class ChangeRequestDto(
    val id: String = "",
    val status: String = "",
    val requestedAt: String = "",
    val organizationId: String = "",
    val userId: String = "",
    val userFullName: String = "Usuário",
    val currentFaceUrl: String? = null,
    val newFaceUrl: String = ""
)

@Serializable
data class ChangeRequestListResponse(
    val success: Boolean = false,
    val message: String = "",
    val data: List<ChangeRequestDto> = emptyList()
)

@Serializable
data class ChangeRequestResponse(
    val success: Boolean = false,
    val message: String = "",
    val data: ChangeRequestDto? = null
)


@Serializable
data class ReviewRequestBody(
    val approved: Boolean
)


class ChangeRequestRepository {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
                coerceInputValues = true
            })
        }
    }


    fun createChangeRequest(
        organizationId: String,
        imageBytes: ByteArray,
        token: String,
        onSuccess: (ChangeRequestDto) -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val httpResponse = client.submitFormWithBinaryData(
                    url = "$BASE_URL/change-requests",
                    formData = formData {
                        append("organizationId", organizationId)
                        append("image", imageBytes, Headers.build {
                            append(HttpHeaders.ContentType, "image/jpeg")
                            append(HttpHeaders.ContentDisposition, "filename=\"new_face.jpg\"")
                        })
                    }
                ) {
                    header("Authorization", "Bearer $token")
                    header("ngrok-skip-browser-warning", "true")
                }

                if (httpResponse.status.value >= 400) {
                    val errorBody = httpResponse.bodyAsText()
                    onError("Erro ${httpResponse.status.value}: $errorBody")
                    return@launch
                }

                val response = httpResponse.body<ChangeRequestResponse>()
                if (response.success && response.data != null) onSuccess(response.data)
                else onError(response.message)

            } catch (e: Exception) {
                onError("Erro de conexão: ${e.message}")
            }
        }
    }


    fun listPendingChangeRequests(
        organizationId: String,
        token: String,
        onSuccess: (List<ChangeRequestDto>) -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val httpResponse = client.get("$BASE_URL/change-requests/organization/$organizationId") {
                    headers {
                        append("ngrok-skip-browser-warning", "true")
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                    contentType(ContentType.Application.Json)
                }

                if (httpResponse.status.value >= 400) {
                    onError("Erro ${httpResponse.status.value}")
                    return@launch
                }

                val response = httpResponse.body<ChangeRequestListResponse>()
                if (response.success) onSuccess(response.data) else onError(response.message)

            } catch (e: Exception) {
                onError("Erro: ${e.message}")
            }
        }
    }

    //revisar
    fun approveChangeRequest(
        requestId: String,
        token: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Chama o endpoint de review passando TRUE
        sendReview(requestId, true, token, onSuccess, onError)
    }

    //revisar - rejeitar
    fun rejectChangeRequest(
        requestId: String,
        token: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Chama o endpoint
        sendReview(requestId, false, token, onSuccess, onError)
    }


    private fun sendReview(
        requestId: String,
        isApproved: Boolean,
        token: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val httpResponse = client.post("$BASE_URL/change-requests/$requestId/review") {
                    headers {
                        append("ngrok-skip-browser-warning", "true")
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                    contentType(ContentType.Application.Json)

                    setBody(ReviewRequestBody(approved = isApproved))
                }

                if (httpResponse.status.value >= 400) {
                    onError("Erro ${httpResponse.status.value}")
                    return@launch
                }

                val response = httpResponse.body<ChangeRequestResponse>()


                if (response.success) {
                    onSuccess()
                } else {
                    onError(response.message)
                }

            } catch (e: Exception) {
                onError(e.message ?: "Erro desconhecido")
            }
        }
    }
}