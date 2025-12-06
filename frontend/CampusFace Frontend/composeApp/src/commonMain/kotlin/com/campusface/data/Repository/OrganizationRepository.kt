package com.campusface.data.Repository

import com.campusface.data.BASE_URL
import com.campusface.data.Model.Organization
import com.campusface.data.Model.OrganizationCreateRequest
import com.campusface.data.Model.OrganizationResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
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
data class OrganizationUpdateBody(
    val name: String,
    val description: String
)
@Serializable
data class OrganizationListResponse(
    val success: Boolean,
    val message: String,
    val data: List<Organization> = emptyList()
)

class OrganizationRepository {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }


    fun createOrganization(
        name: String,
        description: String,
        hubCode: String,
        token: String?,
        onSuccess: (Organization) -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                println("Criando Org: $name ($hubCode)")

                val httpResponse = client.post(BASE_URL + "/organizations") {
                    headers {
                        append("ngrok-skip-browser-warning", "true")
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                    contentType(ContentType.Application.Json)
                    setBody(
                        OrganizationCreateRequest(
                            name = name,
                            description = description,
                            hubCode = hubCode
                        )
                    )
                }

                if (httpResponse.status.value >= 400) {
                    val errorBody = httpResponse.bodyAsText()
                    onError("Erro ${httpResponse.status.value}: $errorBody")
                    return@launch
                }

                val response = httpResponse.body<OrganizationResponse>()

                if (response.success && response.data != null) {
                    onSuccess(response.data)
                } else {
                    onError(response.message)
                }

            } catch (e: Exception) {
                println("CreateOrganization ERROR: ${e.message}")
                e.printStackTrace()
                onError("Erro de conexão: ${e.message}")
            }
        }
    }


    fun getMyHubs(
        token: String?,
        onSuccess: (List<Organization>) -> Unit,
        onError: (String) -> Unit
    ) {

        if (token.isNullOrBlank()) {
            onError("Token inválido ou não encontrado.")
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                println("Buscando meus hubs...")


                val httpResponse = client.get(BASE_URL + "/organizations/my-hubs") {
                    headers {
                        append("ngrok-skip-browser-warning", "true")
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                    contentType(ContentType.Application.Json)
                }


                if (httpResponse.status.value >= 400) {
                    val errorBody = httpResponse.bodyAsText()
                    onError("Erro ${httpResponse.status.value}: $errorBody")
                    return@launch
                }


                val response = httpResponse.body<OrganizationListResponse>()

                if (response.success) {
                    onSuccess(response.data)
                } else {
                    onError(response.message)
                }

            } catch (e: Exception) {
                println("GetMyHubs ERROR: ${e.message}")
                e.printStackTrace()
                onError("Erro de conexão: ${e.message}")
            }
        }
    }

    fun updateOrganization(
        id: String,
        name: String,
        description: String,
        token: String,
        onSuccess: (Organization) -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val httpResponse = client.put(BASE_URL + "/organizations/$id") {
                    headers {
                        append("ngrok-skip-browser-warning", "true")
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                    contentType(ContentType.Application.Json)
                    setBody(OrganizationUpdateBody(name, description))
                }

                if (httpResponse.status.value >= 400) {
                    onError("Erro ${httpResponse.status.value}")
                    return@launch
                }

                val response = httpResponse.body<OrganizationResponse>()
                if (response.success && response.data != null) {
                    onSuccess(response.data)
                } else {
                    onError(response.message)
                }
            } catch (e: Exception) {
                onError("Erro: ${e.message}")
            }
        }
    }

    fun deleteOrganization(
        id: String,
        token: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val httpResponse = client.delete(BASE_URL + "/organizations/$id") {
                    headers {
                        append("ngrok-skip-browser-warning", "true")
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }

                if (httpResponse.status.value >= 400) {
                    onError("Erro ${httpResponse.status.value}")
                    return@launch
                }


                onSuccess()
            } catch (e: Exception) {
                onError("Erro: ${e.message}")
            }
        }
    }

}