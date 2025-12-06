package com.campusface.data.Repository

import com.campusface.data.BASE_URL
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
data class ValidateCodeRequest(
    val code: String
)

@Serializable
data class ValidationResponseData(
    val valid: Boolean,
    val message: String,
    val member: OrganizationMember? = null
)

@Serializable
data class ValidationApiResponse(
    val success: Boolean,
    val message: String,
    val data: ValidationResponseData? = null
)
@Serializable
data class GenerateCodeRequest(
    val organizationId: String
)

@Serializable
data class GeneratedCodeData(
    val code: String,
    val expirationTime: String
)

@Serializable
data class GenerateCodeResponse(
    val success: Boolean,
    val message: String,
    val data: GeneratedCodeData? = null
)



class ValidationRepository {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    fun generateQrCode(
        organizationId: String,
        token: String,
        onSuccess: (GeneratedCodeData) -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {

                val httpResponse = client.post(BASE_URL + "/validate/qr-code/generate") {
                    headers {
                        append("ngrok-skip-browser-warning", "true")
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                    contentType(ContentType.Application.Json)

                    setBody(GenerateCodeRequest(organizationId))
                }

                if (httpResponse.status.value >= 400) {
                    val raw = httpResponse.bodyAsText()
                    onError("Erro ${httpResponse.status.value}: $raw")
                    return@launch
                }

                val response = httpResponse.body<GenerateCodeResponse>()

                if (response.success && response.data != null) {
                    onSuccess(response.data)
                } else {
                    onError(response.message)
                }

            } catch (e: Exception) {
                onError("Erro de conexão: ${e.message}")
            }
        }
    }
    fun validateQrCode(
        code: String,
        token: String,
        onSuccess: (ValidationResponseData) -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val httpResponse = client.post(BASE_URL + "/validate/qr-code") {
                    headers {
                        append("ngrok-skip-browser-warning", "true")
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                    contentType(ContentType.Application.Json)
                    setBody(ValidateCodeRequest(code))
                }


                val rawResponse = httpResponse.body<ValidationApiResponse>()

                if (rawResponse.success && rawResponse.data != null) {
                    onSuccess(rawResponse.data)
                } else {

                    onError(rawResponse.message)
                }

            } catch (e: Exception) {

                onError("Erro de validação: ${e.message}")
            }
        }
    }
}