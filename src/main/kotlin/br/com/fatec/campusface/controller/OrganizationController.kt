package br.com.fatec.campusface.controller

import br.com.fatec.campusface.dto.*
import br.com.fatec.campusface.models.Organization
import br.com.fatec.campusface.models.User
import br.com.fatec.campusface.service.OrganizationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.lang.IllegalArgumentException

@RestController
@RequestMapping("/organizations")
class OrganizationController(private val organizationService: OrganizationService) {


}