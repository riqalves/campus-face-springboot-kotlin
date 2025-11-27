package br.com.fatec.campusface.service

import br.com.fatec.campusface.models.ChangeRequest
import br.com.fatec.campusface.repository.ChangeRequestRepository
import br.com.fatec.campusface.repository.OrganizationMemberRepository
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.lang.IllegalStateException

@Service
class ChangeRequestService(
    private val changeRequestRepository: ChangeRequestRepository,
    private val orgMemberRepository: OrganizationMemberRepository,
    private val cloudinaryService: CloudinaryService
) {


}