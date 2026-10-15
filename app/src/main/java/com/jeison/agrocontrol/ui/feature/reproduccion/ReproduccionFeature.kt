package com.jeison.agrocontrol.ui.feature.reproduccion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeison.agrocontrol.data.ReproductionEventLocal
import com.jeison.agrocontrol.di.AppContainer
import com.jeison.agrocontrol.di.AppViewModelFactory
import com.jeison.agrocontrol.ui.common.Formatters
import com.jeison.agrocontrol.ui.components.AgroCard
import com.jeison.agrocontrol.ui.components.AgroFabExtended
import com.jeison.agrocontrol.ui.components.AgroScaffold
import com.jeison.agrocontrol.ui.components.KpiAccent
import com.jeison.agrocontrol.ui.components.KpiTile
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

enum class ReproFilter { ALL, CELO, INSEMINACION, DIAGNOSTICO, PARTO }

data class ReproRowUi(
    val codigo: String,
    val title: String,
    val raza: String,
    val estado: String,
    val lastEvent: String,
    val lastNotas: String = "",
)

data class ReproduccionUiState(
    val enCelo: String = "0",
    val inseminadas: String = "0",
    val gestantes: String = "0",
    val partos: String = "0",
    val filter: ReproFilter = ReproFilter.ALL,
    val rows: List<ReproRowUi> = emptyList(),
    val loading: Boolean = true,
)

class ReproduccionViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(ReproduccionUiState())
    val state: StateFlow<ReproduccionUiState> = _state.asStateFlow()

    init { refresh() }

    fun setFilter(filter: ReproFilter) {
        _state.update { it.copy(filter = filter) }
        viewModelScope.launch { rebuildRows() }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            container.syncStatus.syncNow()
            runCatching { container.api.reproductionSummary() }.onSuccess { s ->
                _state.update {
                    it.copy(
                        enCelo = Formatters.formatInteger(s.enCelo),
                        inseminadas = Formatters.formatInteger(s.inseminadas),
                        gestantes = Formatters.formatInteger(s.gestantes),
                        partos = Formatters.formatInteger(s.partosMes),
                    )
                }
            }
            rebuildRows()
            _state.update { it.copy(loading = false) }
        }
    }

    private suspend fun rebuildRows() {
        val animals = container.animalRepository.getAll().filter { it.sexo.equals("Hembra", true) }
        val events = container.localCacheDao.allReproduction()
        val byAnimal = events.groupBy { it.animalCodigo }
        val rows = animals.map { a ->
            val last = byAnimal[a.codigo]?.maxByOrNull { it.programada }
            ReproRowUi(
                codigo = a.codigo,
                title = animalDisplayTitle(a),
                raza = a.raza,
                estado = mapReproEstadoFromLast(last),
                lastEvent = last?.let { "${reproductionEventLabel(it.tipoEvento)} · ${Formatters.formatDate(it.programada)}" } ?: "Sin eventos",
                lastNotas = last?.notas.orEmpty(),
            )
        }
        val filtered = when (_state.value.filter) {
            ReproFilter.ALL -> rows
            ReproFilter.CELO -> rows.filter { it.estado == "En celo" }
            ReproFilter.INSEMINACION -> rows.filter { it.estado == "Inseminada" }
            ReproFilter.DIAGNOSTICO -> rows.filter { it.estado == "Gestante" }
            ReproFilter.PARTO -> rows.filter { it.estado == "Parida" }
        }
        _state.update { it.copy(rows = filtered) }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReproduccionRoute(
    onBack: (() -> Unit)? = null,
    onRegistrar: () -> Unit,
    onDetalle: (String) -> Unit = {},
    vm: ReproduccionViewModel = viewModel(factory = AppViewModelFactory),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    AgroScaffold(
        title = "Reproducción",
        onBack = onBack,
        isLoading = state.loading,
        fab = { AgroFabExtended("Registrar evento", onRegistrar) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(AgroSpacing.md),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(AgroSpacing.sm)) {
                    KpiTile(AgroIcons.Reproduccion, "En celo", state.enCelo, accent = KpiAccent.TERTIARY, modifier = Modifier.weight(1f), onClick = { vm.setFilter(ReproFilter.CELO) })
                    KpiTile(AgroIcons.Reproduccion, "Inseminadas", state.inseminadas, accent = KpiAccent.PRIMARY, modifier = Modifier.weight(1f), onClick = { vm.setFilter(ReproFilter.INSEMINACION) })
                }
                Spacer(Modifier.padding(AgroSpacing.xxs))
                Row(horizontalArrangement = Arrangement.spacedBy(AgroSpacing.sm)) {
                    KpiTile(AgroIcons.Reproduccion, "Gestantes", state.gestantes, accent = KpiAccent.SECONDARY, modifier = Modifier.weight(1f), onClick = { vm.setFilter(ReproFilter.DIAGNOSTICO) })
                    KpiTile(AgroIcons.Reproduccion, "Partos", state.partos, accent = KpiAccent.ERROR, modifier = Modifier.weight(1f), onClick = { vm.setFilter(ReproFilter.PARTO) })
                }
                SectionHeader("Hembras", actionLabel = "Todas", onAction = { vm.setFilter(ReproFilter.ALL) })
            }
            items(state.rows, key = { it.codigo }) { row ->
                AgroCard(
                    Modifier.fillMaxWidth().padding(vertical = AgroSpacing.xxs),
                    onClick = { onDetalle(row.codigo) },
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(row.title, style = MaterialTheme.typography.titleSmall)
                            Text("${row.raza} · ${row.codigo}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(row.lastEvent, style = MaterialTheme.typography.labelMedium)
                            if (row.lastNotas.isNotBlank()) {
                                Text(
                                    row.lastNotas,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                )
                            }
                        }
                        StatusChip(row.estado, StatusChipTone.INFO)
                    }
                }
            }
        }
    }
}
