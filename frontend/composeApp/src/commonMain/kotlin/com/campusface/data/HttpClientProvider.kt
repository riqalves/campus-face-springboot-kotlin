package com.campusface.data

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

object HttpClientProvider {

    // Cliente sem autenticação
    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }
    }

    // Cliente com autenticação
    fun authenticatedClient(token: String) = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }

        defaultRequest {
            header(HttpHeaders.Authorization, "Bearer $token")
            header("ngrok-skip-browser-warning", "true")
        }
    }
}