package br.com.fatec.campusface.controller

import br.com.fatec.campusface.dto.ApiResponse
import br.com.fatec.campusface.service.ChangeRequestService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.security.Principal

data class ReviewAction(val action: String) // "APPROVE" ou "DENY"

@RestController
@RequestMapping("/api/change-requests")
class ChangeRequestController(
    private val changeRequestService: ChangeRequestService
) {


    // Você também pode criar um endpoint GET para o admin listar as solicitações pendentes.
}