package br.com.fatec.campusface.config

import com.azure.ai.vision.face.FaceClient
import com.azure.ai.vision.face.FaceClientBuilder
import com.azure.core.credential.AzureKeyCredential
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AzureFaceConfig {

    @Value("\${azure.face.endpoint}")
    private lateinit var endpoint: String

    @Value("\${azure.face.key}")
    private lateinit var key: String

    /**
     * Cria e configura o Bean do FaceClient da Azure.
     * Este cliente será usado para todas as interações com a API do Azure Face.
     */
    @Bean
    fun faceClient(): FaceClient {
        return FaceClientBuilder()
            .endpoint(endpoint)
            .credential(AzureKeyCredential(key))
            .buildClient()
    }
}