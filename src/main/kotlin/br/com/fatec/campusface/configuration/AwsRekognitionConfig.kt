package br.com.fatec.campusface.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.rekognition.RekognitionClient

@Configuration
class AwsRekognitionConfig {

    @Value("\${aws.region}")
    private lateinit var awsRegion: String

    @Value("\${aws.access-key-id}")
    private lateinit var awsAccessKeyId: String

    @Value("\${aws.secret-access-key}")
    private lateinit var awsSecretAccessKey: String

    @Bean
    fun rekognitionClient(): RekognitionClient {
        val credentials = AwsBasicCredentials.create(awsAccessKeyId, awsSecretAccessKey)

        return RekognitionClient.builder()
            .region(Region.of(awsRegion))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build()
    }
}