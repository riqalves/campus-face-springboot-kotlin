package com.campusface.screens.validarScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import com.campusface.navigation.DashboardRoute
import com.campusface.data.Model.Organization
import com.campusface.data.Repository.EntryRequest
import com.campusface.data.Repository.EntryRequestRepository
import com.campusface.data.Repository.LocalAuthRepository
import com.campusface.data.Repository.OrganizationRepository
import com.campusface.utils.AppEventBus // Certifique-se de que este arquivo existe


data class ValidarUiState(
    val isLoading: Boolean = false,
    val activeValidatorHubs: List<Organization> = emptyList(), // Onde já sou validador
    val pendingValidatorRequests: List<EntryRequest> = emptyList(), // Onde pedi pra ser validador
    val error: String? = null
)

class ValidarViewModel(
    private val orgRepo: OrganizationRepository = OrganizationRepository(),
    private val entryRepo: EntryRequestRepository = EntryRequestRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ValidarUiState())
    val uiState = _uiState.asStateFlow()

    private var isLoaded = false


    private var savedToken: String? = null
    private var savedUserId: String? = null

    init {

        viewModelScope.launch {
            AppEventBus.refreshFlow.collect {
                if (!savedToken.isNullOrBlank() && !savedUserId.isNullOrBlank()) {
                    fetchValidatorData(savedToken, savedUserId, forceReload = true)
                }
            }
        }
    }

    fun fetchValidatorData(token: String?, currentUserId: String?, forceReload: Boolean = false) {
        if (token.isNullOrBlank() || currentUserId.isNullOrBlank()) return

        savedToken = token
        savedUserId = currentUserId

        if (isLoaded && !forceReload) return

        _uiState.update { it.copy(isLoading = true, error = null) }


        orgRepo.getMyHubs(
            token = token,
            onSuccess = { allHubs ->

                val myValidatorHubs = allHubs.filter { org ->
                    org.validators.any { user -> user.id == currentUserId }
                }


                fetchRequests(token, myValidatorHubs)
            },
            onError = { error ->

                fetchRequests(token, emptyList(), error)
            }
        )
    }

    private fun fetchRequests(token: String, currentHubs: List<Organization>, previousError: String? = null) {
        entryRepo.listMyRequests(
            token = token,
            onSuccess = { allRequests ->
                isLoaded = true


                val validatorRequests = allRequests.filter {
                    it.role == "VALIDATOR"
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        activeValidatorHubs = currentHubs,
                        pendingValidatorRequests = validatorRequests,
                        error = previousError // Mantém erro da primeira chamada se houve
                    )
                }
            },
            onError = { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        activeValidatorHubs = currentHubs,
                        pendingValidatorRequests = emptyList(),
                        error = previousError ?: error
                    )
                }
            }
        )
    }
    fun refresh(token: String?, userId: String?) {

        fetchValidatorData(token, userId, forceReload = true)
    }
}



@Composable
fun ValidarScreen(
    navController: NavHostController,
    viewModel: ValidarViewModel = viewModel { ValidarViewModel() }
) {
    val authRepository = LocalAuthRepository.current
    val authState by authRepository.authState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()


    LaunchedEffect(Unit) {
        viewModel.fetchValidatorData(authState.token, authState.user?.id)
    }

    val onRefresh: () -> Unit = {
        viewModel.refresh(authState.token, authState.user?.id)
    }

    AdaptiveScreenContainer {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {


            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Área do Validador", style = MaterialTheme.typography.titleMedium)

                Button(
                    onClick = {

                        navController.navigate(DashboardRoute.AdicionarMembro(role = "VALIDATOR"))
                    },
                ) {
                    Text("Adicionar", style = MaterialTheme.typography.labelMedium)
                }
            }


            PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = onRefresh,
                modifier = Modifier.weight(1f)
            ) {
                UnifiedValidatorList(
                    activeHubs = uiState.activeValidatorHubs,
                    pendingRequests = uiState.pendingValidatorRequests,
                    navController = navController,
                    error = uiState.error,
                    onRetry = onRefresh
                )
            }
        }
    }
}



@Composable
fun UnifiedValidatorList(
    activeHubs: List<Organization>,
    pendingRequests: List<EntryRequest>,
    navController: NavHostController,
    error: String?,
    onRetry: () -> Unit
) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {


            if (error != null) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Erro: $error",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                        IconButton(onClick = onRetry) { Icon(Icons.Default.Refresh, "Recarregar") }
                    }
                }
            }


            if (activeHubs.isNotEmpty()) {
                items(activeHubs) { org ->
                    ValidatorCard(
                        title = org.name,
                        subtitle = org.hubCode,
                        status = "Ativo",
                        statusColor = Color(0xFF00A12B), // Verde
                        isClickable = true,
                        onClick = {

                            navController.navigate(DashboardRoute.QrCodeValidador)
                        }
                    )
                }
            }


            val visibleRequests = pendingRequests.filter { it.status != "APPROVED" }

            if (visibleRequests.isNotEmpty()) {
                items(visibleRequests) { req ->
                    val (color, text) = when (req.status) {
                        "PENDING" -> Color(0xFFFFBB00) to "Solicitado"
                        "DENIED" -> Color(0xFFB00020) to "Recusado"
                        else -> Color.Gray to req.status
                    }

                    ValidatorCard(
                        title = req.hubCode,
                        subtitle = "Aguardando aprovação",
                        status = text,
                        statusColor = color,
                        isClickable = false,
                        onClick = {}
                    )
                }
            }


            if (activeHubs.isEmpty() && visibleRequests.isEmpty() && error == null) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "Você não é validador em nenhum Hub.",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
}

@Composable
fun ValidatorCard(
    title: String,
    subtitle: String,
    status: String,
    statusColor: Color,
    isClickable: Boolean,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .then(if (isClickable) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(

            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                if (isClickable) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scanner",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Box(modifier = Modifier.size(10.dp).background(statusColor, CircleShape))

                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}