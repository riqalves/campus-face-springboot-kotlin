package br.com.fatec.campusface.models

data class OrganizationMember(
    val id: String = "",
    val organizationId: String = "",
    val userId: String? = "",
    // Este campo agora armazena o public_id da imagem no Cloudinary (a fonte da verdade)
    val faceImageId: String? = "",

    // CAMPO ADICIONADO: Armazena o token do rosto deste membro no Face++.
    // Este token é usado para identificar o match na API de busca.
    val faceToken: String? = null
)