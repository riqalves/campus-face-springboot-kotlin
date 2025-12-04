package com.campusface.screens.administrarScreen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// --- IMPORTS DO SEU PROJETO ---
import com.campusface.components.AdaptiveScreenContainer
import com.campusface.data.Repository.LocalAuthRepository
import com.campusface.data.Repository.OrganizationMemberRepository
import com.campusface.data.Repository.OrganizationMember
import com.campusface.data.Repository.EntryRequestRepository
import com.campusface.data.Repository.EntryRequest
import com.campusface.data.Repository.OrganizationRepository
import com.campusface.data.Model.Organization
import com.campusface.data.Repository.ChangeRequestRepository
import com.campusface.data.Repository.ChangeRequestDto
import com.campusface.utils.AppEventBus

fun buildImageUrl(imageId: String?): String {
    if (imageId.isNullOrBlank()) return ""
    return if (imageId.startsWith("http")) {
        imageId
    } else {
        "https://res.cloudinary.com/dt2117/image/upload/$imageId"
    }
}
data class HubDetailsUiState(
    val isLoading: Boolean = false,
    val hub: Organization? = null,
    val membros: List<OrganizationMember> = emptyList(),
    val entryRequests: List<EntryRequest> = emptyList(),
    val changeRequests: List<ChangeRequestDto> = emptyList(),
    val error: String? = null
)

class HubDetailsViewModel(
    private val memberRepo: OrganizationMemberRepository = OrganizationMemberRepository(),
    private val entryRepo: EntryRequestRepository = EntryRequestRepository(),
    private val orgRepo: OrganizationRepository = OrganizationRepository(),
    private val changeRepo: ChangeRequestRepository = ChangeRequestRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HubDetailsUiState())
    val uiState = _uiState.asStateFlow()

    private var currentHubCode: String? = null
    private var savedHubId: String? = null
    private var savedToken: String? = null

    init {

        viewModelScope.launch {
            AppEventBus.refreshFlow.collect {
                // Se temos os dados salvos, recarregamos tudo
                if (!savedHubId.isNullOrBlank() && !savedToken.isNullOrBlank()) {
                    fetchHubDetails(savedHubId!!, savedToken!!, forceReload = true)
                }
            }
        }
    }

    fun fetchHubDetails(hubId: String, token: String?, forceReload: Boolean = false) {
        if (token.isNullOrBlank()) return
        savedHubId = hubId
        savedToken = token
        _uiState.update { it.copy(isLoading = true, error = null) }


        memberRepo.listMembers(
            organizationId = hubId,
            token = token,
            onSuccess = { listaMembros ->
                _uiState.update { it.copy(membros = listaMembros) }


                orgRepo.getMyHubs(
                    token = token,
                    onSuccess = { hubs ->
                        val hubEncontrado = hubs.find { it.id == hubId }
                        if (hubEncontrado != null) {
                            currentHubCode = hubEncontrado.hubCode
                            _uiState.update { it.copy(hub = hubEncontrado) }


                            fetchEntryRequests(hubEncontrado.hubCode, token)


                            fetchChangeRequests(hubId, token)

                        } else {
                            _uiState.update { it.copy(isLoading = false, error = "Hub não encontrado") }
                        }
                    },
                    onError = { erro -> _uiState.update { it.copy(isLoading = false, error = erro) } }
                )
            },
            onError = { erro -> _uiState.update { it.copy(isLoading = false, error = erro) } }
        )
    }

    private fun fetchEntryRequests(hubCode: String, token: String) {
        entryRepo.listPendingRequestsByHub(
            hubCode = hubCode,
            token = token,
            onSuccess = { requests ->
                _uiState.update { it.copy(isLoading = false, entryRequests = requests) }
            },
            onError = { _uiState.update { it.copy(isLoading = false) } }
        )
    }

    private fun fetchChangeRequests(hubId: String, token: String) {
        changeRepo.listPendingChangeRequests(
            organizationId = hubId,
            token = token,
            onSuccess = { requests ->
                _uiState.update { it.copy(isLoading = false, changeRequests = requests) }
            },
            onError = { _uiState.update { it.copy(isLoading = false) } }
        )
    }



    fun removerMembro(memberId: String, token: String) {
        if (token.isEmpty()) return
        val backup = _uiState.value.membros
        _uiState.update { it.copy(membros = it.membros.filter { m -> m.id != memberId }) }

        memberRepo.deleteMember(
            memberId = memberId,
            token = token,
            onSuccess = { viewModelScope.launch { AppEventBus.emitRefresh() } },
            onError = { msg ->
                _uiState.update { it.copy(membros = backup, error = "Erro ao remover: $msg") }
            }
        )
    }

    fun atualizarMembro(memberId: String, newRole: String? = null, newStatus: String? = null, token: String) {
        if (token.isEmpty()) return
        val backup = _uiState.value.membros


        _uiState.update { state ->
            state.copy(membros = state.membros.map { m ->
                if (m.id == memberId) m.copy(role = newRole ?: m.role, status = newStatus ?: m.status) else m
            })
        }

        memberRepo.updateMember(
            memberId = memberId,
            newRole = newRole,
            newStatus = newStatus,
            token = token,
            onSuccess = { viewModelScope.launch { AppEventBus.emitRefresh() } },
            onError = { msg -> _uiState.update { it.copy(membros = backup, error = "Erro ao atualizar: $msg") } }
        )
    }


    fun aprovarEntry(requestId: String, token: String) {
        if (token.isEmpty()) return
        val backup = _uiState.value.entryRequests
        _uiState.update { it.copy(entryRequests = it.entryRequests.filter { r -> r.id != requestId }) }
        entryRepo.approveRequest(requestId, token, onSuccess = {viewModelScope.launch { AppEventBus.emitRefresh() }}, onError = { msg -> _uiState.update { it.copy(entryRequests = backup, error = msg) } })
    }

    fun recusarEntry(requestId: String, token: String) {
        if (token.isEmpty()) return
        val backup = _uiState.value.entryRequests
        _uiState.update { it.copy(entryRequests = it.entryRequests.filter { r -> r.id != requestId }) }
        entryRepo.rejectRequest(requestId, token, onSuccess = {viewModelScope.launch { AppEventBus.emitRefresh() }}, onError = { msg -> _uiState.update { it.copy(entryRequests = backup, error = msg) } })
    }

    fun aprovarChange(requestId: String, token: String) {
        if (token.isEmpty()) return
        val backup = _uiState.value.changeRequests
        _uiState.update { it.copy(changeRequests = it.changeRequests.filter { r -> r.id != requestId }) }
        changeRepo.approveChangeRequest(requestId, token, onSuccess = {viewModelScope.launch { AppEventBus.emitRefresh() }}, onError = { msg -> _uiState.update { it.copy(changeRequests = backup, error = msg) } })
    }

    fun recusarChange(requestId: String, token: String) {
        if (token.isEmpty()) return
        val backup = _uiState.value.changeRequests
        _uiState.update { it.copy(changeRequests = it.changeRequests.filter { r -> r.id != requestId }) }
        changeRepo.rejectChangeRequest(requestId, token, onSuccess = {viewModelScope.launch { AppEventBus.emitRefresh() }}, onError = { msg -> _uiState.update { it.copy(changeRequests = backup, error = msg) } })
    }
}


@Composable
fun DetalhesHubScreen(
    hubId: String,
    navController: NavHostController,
    viewModel: HubDetailsViewModel = viewModel { HubDetailsViewModel() }
) {
    val authRepository = LocalAuthRepository.current
    val authState by authRepository.authState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(hubId) {
        viewModel.fetchHubDetails(hubId, authState.token)
    }

    AdaptiveScreenContainer {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                }
                Spacer(Modifier.width(8.dp))
                Text(text = uiState.hub?.name ?: "Carregando...", style = MaterialTheme.typography.titleMedium)
            }

            // Conteúdo
            if (uiState.isLoading && uiState.hub == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (uiState.error != null && uiState.hub == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Erro: ${uiState.error}", color = MaterialTheme.colorScheme.error) }
            } else {
                HubTabsContent(
                    membros = uiState.membros,
                    solicitacoes = uiState.entryRequests,
                    changes = uiState.changeRequests,
                    onDeleteMembro = { id -> viewModel.removerMembro(id, authState.token ?: "") },
                    onUpdateMembro = { id, role, status -> viewModel.atualizarMembro(id, role, status, authState.token ?: "") },
                    onAprovarEntry = { id -> viewModel.aprovarEntry(id, authState.token ?: "") },
                    onRecusarEntry = { id -> viewModel.recusarEntry(id, authState.token ?: "") },
                    onAprovarChange = { id -> viewModel.aprovarChange(id, authState.token ?: "") },
                    onRecusarChange = { id -> viewModel.recusarChange(id, authState.token ?: "") }
                )
            }
        }
    }
}

@Composable
fun HubTabsContent(
    membros: List<OrganizationMember>,
    solicitacoes: List<EntryRequest>,
    changes: List<ChangeRequestDto>,
    onDeleteMembro: (String) -> Unit,
    onUpdateMembro: (String, String?, String?) -> Unit,
    onAprovarEntry: (String) -> Unit,
    onRecusarEntry: (String) -> Unit,
    onAprovarChange: (String) -> Unit,
    onRecusarChange: (String) -> Unit
) {
    val tabs = listOf("Membros", "Entrada", "Fotos")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column {
        PrimaryTabRow(selectedTabIndex = selectedTabIndex, containerColor = Color.Transparent) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        if (index == 1 && solicitacoes.isNotEmpty()) Text("$title (${solicitacoes.size})")
                        else if (index == 2 && changes.isNotEmpty()) Text("$title (${changes.size})")
                        else Text(title)
                    }
                )
            }
        }

        Crossfade(targetState = selectedTabIndex, label = "Tabs") { index ->
            when (index) {
                0 -> TabContentMembros(membros, onDeleteMembro, onUpdateMembro)
                1 -> TabContentSolicitacaoEntrada(solicitacoes, onAprovarEntry, onRecusarEntry)
                2 -> TabContentChangeRequests(changes, onAprovarChange, onRecusarChange)
            }
        }
    }
}


@Composable
fun TabContentMembros(
    listaMembros: List<OrganizationMember>,
    onDelete: (String) -> Unit,
    onUpdate: (String, String?, String?) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (listaMembros.isEmpty()) {
            item { Text("Nenhum membro encontrado.", modifier = Modifier.padding(16.dp)) }
        } else {
            items(listaMembros) { membro ->
                MembroCard(
                    member = membro,
                    onDelete = { onDelete(membro.id) },
                    onUpdate = { role, status -> onUpdate(membro.id, role, status) }
                )
            }
        }
    }
}

@Composable
fun MembroCard(
    member: OrganizationMember,
    onDelete: () -> Unit,
    onUpdate: (String?, String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isBanned = member.status == "INACTIVE"

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isBanned) Color(0xFFEEEEEE) else MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PhotoCircle(url = buildImageUrl(member.user.faceImageId))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.user.fullName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isBanned) Color.Gray else Color.Unspecified
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = member.role,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    if (isBanned) {
                        Spacer(Modifier.width(8.dp))
                        Text("(BANIDO)", style = MaterialTheme.typography.labelSmall, color = Color.Red)
                    }
                }
            }


            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = Color.White
                ) {
                    // Opções de Role
                    if (member.role != "VALIDATOR") {
                        DropdownMenuItem(
                            text = { Text("Promover a Validador") },
                            onClick = { expanded = false; onUpdate("VALIDATOR", null) },
                            leadingIcon = { Icon(Icons.Default.Security, null) }
                        )
                    }
                    if (member.role != "MEMBER") {
                        DropdownMenuItem(
                            text = { Text("Rebaixar a Membro") },
                            onClick = { expanded = false; onUpdate("MEMBER", null) },
                            leadingIcon = { Icon(Icons.Default.Person, null) }
                        )
                    }

                    HorizontalDivider()

                    // Opções de Status
                    if (member.status == "ACTIVE") {
                        DropdownMenuItem(
                            text = { Text("Banir Acesso", color = Color.Red) },
                            onClick = { expanded = false; onUpdate(null, "INACTIVE") },
                            leadingIcon = { Icon(Icons.Default.Block, null, tint = Color.Red) }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Ativar Acesso", color = Color(0xFF00A12B)) },
                            onClick = { expanded = false; onUpdate(null, "ACTIVE") },
                            leadingIcon = { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF00A12B)) }
                        )
                    }

                    HorizontalDivider()

                    // Excluir
                    DropdownMenuItem(
                        text = { Text("Excluir", color = Color.Red) },
                        onClick = { expanded = false; onDelete() },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                    )
                }
            }
        }
    }
}


@Composable
fun TabContentSolicitacaoEntrada(
    listaSolicitacoes: List<EntryRequest>,
    onAceitar: (String) -> Unit,
    onRecusar: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (listaSolicitacoes.isEmpty()) {
            item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("Nenhuma solicitação pendente.", color = Color.Gray) } }
        } else {
            items(listaSolicitacoes) { solicitacao ->
                SolicitacaoCard(solicitacao=solicitacao, { onAceitar(solicitacao.id) }, {onRecusar(solicitacao.id)})
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun SolicitacaoCard(solicitacao: EntryRequest, onAceitar: () -> Unit, onRecusar: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(Color.Transparent)) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PhotoCircle(url = buildImageUrl(solicitacao.user.faceImageId))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = solicitacao.user.fullName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(text = "Cargo: ${solicitacao.role}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    solicitacao.user.document?.let { Text(text = it, style = MaterialTheme.typography.labelSmall, color = Color.Gray) }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(onClick = onRecusar, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Recusar") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onAceitar, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A12B))) { Text("Aceitar") }
            }
        }
    }
}

@Composable
fun TabContentChangeRequests(
    lista: List<ChangeRequestDto>,
    onAprovar: (String) -> Unit,
    onRecusar: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (lista.isEmpty()) {
            item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("Nenhuma solicitação de troca.", color = Color.Gray) } }
        } else {
            items(lista) { item ->
                ChangeRequestCard(item, { onAprovar(item.id) }, { onRecusar(item.id) })
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun ChangeRequestCard(request: ChangeRequestDto, onAprovar: () -> Unit, onRecusar: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(Color.Transparent)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = "${request.userFullName} quer mudar a foto", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Atual", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    PhotoCircle(url = buildImageUrl(request.currentFaceUrl))
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForward, "Mudar", modifier = Modifier.padding(horizontal = 16.dp), tint = Color.Gray)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Nova", style = MaterialTheme.typography.labelSmall, color = Color(0xFF00A12B))
                    PhotoCircle(url = buildImageUrl(request.newFaceUrl))
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(onClick = onRecusar, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Recusar") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onAprovar, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A12B))) { Text("Aprovar") }
            }
        }
    }
}

@Composable
fun PhotoCircle(tamanho: Dp = 60.dp, url: String) {
    AsyncImage(
        modifier = Modifier.size(tamanho).clip(CircleShape).background(Color(0xFFE0E0E0)),
        model = url,
        contentDescription = "Foto",
        contentScale = ContentScale.Crop,
        error = ColorPainter(Color.Gray),
        placeholder = ColorPainter(Color.LightGray)
    )
}