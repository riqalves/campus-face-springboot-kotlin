package br.com.fatec.campusface.service

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class CloudinaryService(
    private val cloudinary: Cloudinary
) {


    fun upload(file: MultipartFile): Map<String, String> {
        val result = cloudinary.uploader().upload(file.bytes, ObjectUtils.emptyMap())
        return mapOf(
            "secure_url" to (result["secure_url"] as String),
            "public_id" to (result["public_id"] as String)
        )
    }

    /**
     * Deleta uma imagem do Cloudinary usando seu public_id.
     */
    fun delete(publicId: String) {
        try {
            // O método 'destroy' é usado para deletar um recurso
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap())
            println("DEBUG - Imagem com public_id '$publicId' deletada do Cloudinary.")
        } catch (e: Exception) {
            println("ERRO - Falha ao deletar a imagem '$publicId' do Cloudinary: ${e.message}")

        }
    }
    }