package com.jeison.agrocontrol.ui.feature.reproduccion

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeison.agrocontrol.data.ReproductionEventLocal
import com.jeison.agrocontrol.di.AppContainer
import com.jeison.agrocontrol.di.AppViewModelFactory
import com.jeison.agrocontrol.ui.components.AgroScaffold
import com.jeison.agrocontrol.ui.components.EmptyState
import com.jeison.agrocontrol.ui.components.PrimaryButton
import com.jeison.agrocontrol.ui.components.SectionHeader
import com.jeison.agrocontrol.ui.components.StatusChip
import com.jeison.agrocontrol.ui.components.StatusChipTone
import com.jeison.agrocontrol.ui.feature.animales.animalDisplayTitle
import com.jeison.agrocontrol.ui.theme.AgroIcons
import com.jeison.agrocontrol.ui.theme.AgroSpacing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReproduccionDetalleUiState(
    val title: String = "",
    val raza: String = "",
    val codigo: String = "",
    val estado: String = "",
    val events: List<ReproductionEventLocal> = emptyList(),
    val loading: Boolean = true,
)

class ReproduccionDetalleViewModel(
    savedStateHandle: SavedStateHandle,
    private val container: AppContainer,
) : ViewModel() {
    private val codigo = savedStateHandle.get<String>("codigo").orEmpty()
    private val _state = MutableStateFlow(ReproduccionDetalleUiState(codigo = codigo))
    val state: StateFlow<ReproduccionDetalleUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            container.syncStatus.syncNow()
            val animal = container.animalRepository.getAll().find { it.codigo == codigo }
            val events = container.localCacheDao.allReproduction()
                .filter { it.animalCodigo == codigo }
                .sortedByDescending { it.programada }
            val last = events.firstOrNull()
            _state.update {
                it.copy(
                    loading = false,
                    title = animal?.let { a -> animalDisplayTitle(a) } ?: codigo,
                    raza = animal?.raza.orEmpty(),
                    estado = mapReproEstadoFromLast(last),
                    events = events,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReproduccionDetalleRoute(
    onBack: () -> Unit,
    onRegistrar: (String) -> Unit,
    vm: ReproduccionDetalleViewModel = viewModel(factory = AppViewModelFactory),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    AgroScaffold(title = state.title.ifBlank { "Reproducción" }, onBack = onBack, isLoading = state.loading) { padding ->
        if (!state.loading && state.title.isBlank() && state.events.isEmpty()) {
            EmptyState(AgroIcons.Reproduccion, "Animal no encontrado", "", modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(AgroSpacing.md),
            ) {
                item {
                    Text(
                        "${state.raza} · ${state.codigo}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    StatusChip(state.estado, StatusChipTone.INFO, modifier = Modifier.padding(vertical = AgroSpacing.sm))
                    PrimaryButton("Registrar evento", { onRegistrar(state.codigo) }, modifier = Modifier.fillMaxWidth())
                    SectionHeader("Historial")
                }
                item {
                    ReproduccionEventList(state.events)
                }
            }
        }
    }
}
