package com.campusface.screens.administrarScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Imports do seu projeto
import com.campusface.components.AdaptiveScreenContainer
import com.campusface.data.Model.Organization
import com.campusface.data.Repository.LocalAuthRepository
import com.campusface.data.Repository.OrganizationRepository
import com.campusface.navigation.DashboardRoute
import com.campusface.utils.AppEventBus // Certifique-se de importar o Bus


data class AdministrarUiState(
    val isLoading: Boolean = false,
    val adminHubs: List<Organization> = emptyList(),
    val error: String? = null
)

class AdministrarViewModel(
    private val repository: OrganizationRepository = OrganizationRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdministrarUiState())
    val uiState = _uiState.asStateFlow()

    private var isLoaded = false

    // Variáveis para permitir o refresh automático
    private var savedToken: String? = null
    private var savedUserId: String? = null

    init {

        viewModelScope.launch {
            AppEventBus.refreshFlow.collect {
                if (!savedToken.isNullOrBlank() && !savedUserId.isNullOrBlank()) {
                    // Força o reload limpando o flag isLoaded
                    isLoaded = false
                    fetchAdminHubs(savedToken, savedUserId)
                }
            }
        }
    }

    fun fetchAdminHubs(token: String?, currentUserId: String?, forceReload: Boolean = false) {
        if (token.isNullOrBlank() || currentUserId.isNullOrBlank()) return

        savedToken = token
        savedUserId = currentUserId

        if (isLoaded && !forceReload) return

        _uiState.update { it.copy(isLoading = true, error = null) }

        repository.getMyHubs(
            token = token,
            onSuccess = { allHubs ->
                isLoaded = true
                val onlyAdminHubs = allHubs.filter { org ->
                    org.admins.any { user -> user.id == currentUserId }
                }
                _uiState.update { it.copy(isLoading = false, adminHubs = onlyAdminHubs) }
            },
            onError = { errorMsg ->
                _uiState.update { it.copy(isLoading = false, error = errorMsg) }
            }
        )
    }

    fun deleteHub(hubId: String, token: String) {
        val backupList = _uiState.value.adminHubs
        _uiState.update { it.copy(adminHubs = it.adminHubs.filter { hub -> hub.id != hubId }) }

        repository.deleteOrganization(
            id = hubId,
            token = token,
            onSuccess = {
                viewModelScope.launch { AppEventBus.emitRefresh() }
            },
            onError = { error ->
                _uiState.update { it.copy(adminHubs = backupList, error = "Erro ao deletar: $error") }
            }
        )
    }

    fun updateHub(id: String, name: String, desc: String, token: String) {
        repository.updateOrganization(
            id = id,
            name = name,
            description = desc,
            token = token,
            onSuccess = { updatedOrg ->
                _uiState.update { state ->
                    val newList = state.adminHubs.map { if (it.id == id) updatedOrg else it }
                    state.copy(adminHubs = newList)
                }
                viewModelScope.launch { AppEventBus.emitRefresh() }
            },
            onError = { error ->
                _uiState.update { it.copy(error = "Erro ao atualizar: $error") }
            }
        )
    }

    fun refresh(token: String?, userId: String?) {
        isLoaded = false
        fetchAdminHubs(token, userId)
    }
}



@Composable
fun AdministrarScreen(
    navController: NavHostController,
    viewModel: AdministrarViewModel = viewModel { AdministrarViewModel() }
) {
    val authRepository = LocalAuthRepository.current
    val authState by authRepository.authState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var selectedHubForEdit by remember { mutableStateOf<Organization?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchAdminHubs(authState.token, authState.user?.id)
    }

    AdaptiveScreenContainer {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //header

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Hubs que administro", style = MaterialTheme.typography.titleMedium)
                Button(onClick = { navController.navigate(DashboardRoute.CriarHub) }) {
                    Text("Criar", style = MaterialTheme.typography.labelMedium)
                }
            }

            //content
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Erro: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                            IconButton(onClick = { viewModel.refresh(authState.token, authState.user?.id) }) {
                                Icon(Icons.Default.Refresh, "Tentar Novamente")
                            }
                        }
                    }
                }
                uiState.adminHubs.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Você ainda não administra nenhum Hub.", color = Color.Gray)
                    }
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(uiState.adminHubs) { org ->
                            HubAdminCard(
                                organization = org,
                                onHubClick = { hubId ->
                                    navController.navigate(DashboardRoute.DetalhesHub(hubId = hubId))
                                },
                                onDelete = { viewModel.deleteHub(org.id, authState.token ?: "") },
                                onEdit = {
                                    selectedHubForEdit = org
                                    showEditDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog && selectedHubForEdit != null) {
        EditHubDialog(
            hub = selectedHubForEdit!!,
            onDismiss = { showEditDialog = false },
            onConfirm = { name, desc ->
                viewModel.updateHub(selectedHubForEdit!!.id, name, desc, authState.token ?: "")
                showEditDialog = false
            }
        )
    }
}


@Composable
fun HubAdminCard(
    organization: Organization,
    onHubClick: (String) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = { onHubClick(organization.id) }),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = organization.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(text = organization.description, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Text(text = organization.hubCode, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Groups, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                val totalPessoas = organization.members.size + organization.validators.size + 1
                Text(text = "$totalPessoas", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 4.dp))
                Spacer(Modifier.width(8.dp))

                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Opções")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        containerColor = Color.White
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            onClick = {
                                expanded = false
                                onEdit()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Excluir", color = Color.Red) },
                            onClick = {
                                expanded = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditHubDialog(
    hub: Organization,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(hub.name) }
    var description by remember { mutableStateOf(hub.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Organização") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, description) }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}