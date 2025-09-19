package br.com.fatec.campusface.dto
import java.time.Instant

data class GenerateCodeRequest(val userId: String)
data class GeneratedCodeResponse(val code: String, val expirationTime: Instant)
data class ValidateCodeRequest(val code: String)