package com.jeison.agrocontrol.ui.feature.perfil

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeison.agrocontrol.di.AppContainer
import com.jeison.agrocontrol.di.AppViewModelFactory
import com.jeison.agrocontrol.network.UserMeResponse
import com.jeison.agrocontrol.ui.components.AgroCard
import com.jeison.agrocontrol.ui.components.AgroScaffold
import com.jeison.agrocontrol.ui.components.EmptyState
import com.jeison.agrocontrol.ui.components.PrimaryButton
import com.jeison.agrocontrol.ui.components.StatRow
import com.jeison.agrocontrol.ui.theme.AgroIcons
import com.jeison.agrocontrol.ui.theme.AgroSpacing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PerfilUiState(
    val profile: UserMeResponse? = null,
    val loading: Boolean = true,
    val error: String? = null,
)

class PerfilViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(PerfilUiState())
    val state: StateFlow<PerfilUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { container.api.me() }
                .onSuccess { me -> _state.update { it.copy(profile = me, loading = false, error = null) } }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            loading = false,
                            error = e.message ?: "No se pudo cargar el perfil",
                        )
                    }
                }
        }
    }
}

@Composable
fun PerfilRoute(onBack: () -> Unit, vm: PerfilViewModel = viewModel(factory = AppViewModelFactory)) {
    val state by vm.state.collectAsStateWithLifecycle()
    AgroScaffold(title = "Perfil", onBack = onBack, isLoading = state.loading) { padding ->
        when {
            state.error != null && state.profile == null -> {
                Column(
                    Modifier.padding(padding).fillMaxSize().padding(AgroSpacing.md),
                ) {
                    EmptyState(AgroIcons.Usuario, "Sin conexión", state.error.orEmpty())
                    PrimaryButton("Reintentar", vm::load, modifier = Modifier.padding(top = AgroSpacing.md))
                }
            }
            state.profile != null -> {
                val p = state.profile!!
                Column(
                    Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(AgroSpacing.md),
                ) {
                    AgroCard(Modifier.fillMaxWidth()) {
                        StatRow(AgroIcons.Usuario, "Nombre", p.nombre)
                        StatRow(AgroIcons.Usuario, "Correo", p.email)
                        StatRow(AgroIcons.Potrero, "Finca", p.finca.nombre)
                    }
                    Text(
                        "Los datos de cuenta se gestionan al registrarte; edición próximamente.",
                        modifier = Modifier.padding(top = AgroSpacing.md),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
