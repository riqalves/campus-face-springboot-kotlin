package br.com.fatec.campusface.dto



import jakarta.validation.constraints.NotBlank

data class RegisteredClientResponseDTO(
    val id: String,
    val hubCode: String,
    val ipAddress: String,
    val port: String,
    val status: String
)

data class ClientCheckinDTO(
    @field:NotBlank(message = "O ID do Hub é obrigatório")
    val hub_id: String,

    @field:NotBlank(message = "O endereço do servidor é obrigatório (IP:PORTA)")
    val server: String,

    val token: String?
)