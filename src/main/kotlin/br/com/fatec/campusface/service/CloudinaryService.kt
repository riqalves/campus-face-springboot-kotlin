package br.com.fatec.campusface.service

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class CloudinaryService(
    private val cloudinary: Cloudinary
) {


    fun upload(file: MultipartFile): String {
        val result = cloudinary.uploader().upload(file.bytes, ObjectUtils.emptyMap())

        return result["secure_url"] as String
    }
}