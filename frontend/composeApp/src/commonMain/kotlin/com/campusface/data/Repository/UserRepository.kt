package com.campusface.data.Repository

import com.campusface.data.BASE_URL
import com.campusface.data.Model.ApiResponse
import com.campusface.data.Model.User
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
data class UserUpdateBody(
    val fullName: String,
    val email: String,
    val document: String
)

class UserRepository {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }
    }

    // get
    suspend fun getUser(id: String, token: String): Result<User> = runCatching {
        val httpResponse = client.get("${BASE_URL}/users/$id") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header("ngrok-skip-browser-warning", "true")
        }

        if (httpResponse.status.value >= 400) throw Exception("Erro ${httpResponse.status.value}")

        val apiResponse = httpResponse.body<ApiResponse<User>>()
        if (apiResponse.success && apiResponse.data != null) apiResponse.data
        else throw Exception(apiResponse.message)
    }

    //ATUALIZAR FOTO
    suspend fun updateProfileImage(
        imageBytes: ByteArray,
        token: String
    ): Result<User> = runCatching {
        val httpResponse = client.submitFormWithBinaryData(
            url = "${BASE_URL}/users/image",
            formData = formData {
                append("image", imageBytes, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"profile.jpg\"")
                })
            }
        ) {
            method = HttpMethod.Patch
            header(HttpHeaders.Authorization, "Bearer $token")
            header("ngrok-skip-browser-warning", "true")
        }

        if (httpResponse.status.value >= 400) throw Exception("Erro upload: ${httpResponse.status.value}")

        val apiResponse = httpResponse.body<ApiResponse<User>>()
        if (apiResponse.success && apiResponse.data != null) apiResponse.data
        else throw Exception(apiResponse.message)
    }


    suspend fun updateUserData(
        fullName: String,
        email: String,
        document: String,
        token: String
    ): Result<User> = runCatching {

        val httpResponse = client.put("${BASE_URL}/users") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header("ngrok-skip-browser-warning", "true")
            contentType(ContentType.Application.Json)
            setBody(UserUpdateBody(fullName, email, document))
        }

        if (httpResponse.status.value >= 400) throw Exception("Erro ${httpResponse.status.value}")

        val apiResponse = httpResponse.body<ApiResponse<User>>()
        if (apiResponse.success && apiResponse.data != null) apiResponse.data
        else throw Exception(apiResponse.message)
    }
    //delete
    suspend fun deleteUser(id: String, token: String): Result<Unit> = runCatching {
        val httpResponse = client.delete("${BASE_URL}/users/$id") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header("ngrok-skip-browser-warning", "true")
        }

        if (httpResponse.status.value >= 400) {
            throw Exception("Erro ao deletar: ${httpResponse.status.value}")
        }


    }
}