package com.jeison.agrocontrol.ui.feature.finanzas

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeison.agrocontrol.di.AppContainer
import com.jeison.agrocontrol.di.AppViewModelFactory
import com.jeison.agrocontrol.ui.common.Formatters
import com.jeison.agrocontrol.ui.components.AgroBarChart
import com.jeison.agrocontrol.ui.components.AgroDonutChart
import com.jeison.agrocontrol.ui.components.AgroScaffold
import com.jeison.agrocontrol.ui.components.ChartSegment
import com.jeison.agrocontrol.ui.components.EmptyState
import com.jeison.agrocontrol.ui.components.ProgressBarRow
import com.jeison.agrocontrol.ui.components.SectionHeader
import com.jeison.agrocontrol.ui.theme.AgroIcons
import com.jeison.agrocontrol.ui.theme.AgroSpacing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FinanzasRow(val label: String, val amount: Double, val color: Color)

data class FinanzasUiState(
    val rows: List<FinanzasRow> = emptyList(),
    val segments: List<ChartSegment> = emptyList(),
    val total: Double = 0.0,
    val recordCount: Int = 0,
    val maxValue: Double = 1.0,
    val loading: Boolean = true,
)

class FinanzasViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(FinanzasUiState())
    val state: StateFlow<FinanzasUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val records = container.localCacheDao.allProduction()
            val grouped = records
                .groupBy { it.productType }
                .mapValues { (_, v) -> v.sumOf { it.cantidad } }
                .toList()
                .sortedByDescending { it.second }
            val palette = listOf(
                Color(0xFF2E6B4F),
                Color(0xFF5C8A72),
                Color(0xFF8FB39E),
                Color(0xFFC4A35A),
            )
            val rows = grouped.mapIndexed { index, (type, amount) ->
                FinanzasRow(type, amount, palette[index % palette.size])
            }
            val segments = rows.map { ChartSegment(it.label, it.amount, it.color) }
            val total = rows.sumOf { it.amount }
            _state.update {
                it.copy(
                    rows = rows,
                    segments = segments,
                    total = total,
                    recordCount = records.size,
                    maxValue = rows.maxOfOrNull { r -> r.amount } ?: 1.0,
                    loading = false,
                )
            }
        }
    }
}

@Composable
fun FinanzasRoute(onBack: (() -> Unit)? = null, vm: FinanzasViewModel = viewModel(factory = AppViewModelFactory)) {
    val state by vm.state.collectAsStateWithLifecycle()
    AgroScaffold(title = "Finanzas / Productividad", onBack = onBack, isLoading = state.loading) { padding ->
        if (state.rows.isEmpty() && !state.loading) {
            EmptyState(
                AgroIcons.Finanzas,
                "Aún no hay datos de producción",
                "Sincroniza para ver la productividad de tu finca.",
                modifier = Modifier.padding(padding).fillMaxSize(),
            )
        } else {
            val chartSummary = state.segments.joinToString { "${it.label} ${Formatters.formatNumber(it.value)}" }
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(AgroSpacing.md),
            ) {
                item {
                    Text(
                        "Totales por rubro (datos locales)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "Basado en ${state.recordCount} registros",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = AgroSpacing.md),
                    )
                }
                item {
                    AgroDonutChart(
                        segments = state.segments,
                        centerTitle = "Productividad",
                        contentDescription = "Composición por rubro: $chartSummary",
                    )
                }
                item {
                    SectionHeader("Comparativa por rubro", modifier = Modifier.padding(top = AgroSpacing.lg))
                    AgroBarChart(
                        segments = state.segments,
                        contentDescription = "Barras por rubro: $chartSummary",
                        modifier = Modifier.padding(vertical = AgroSpacing.sm),
                    )
                }
                item { SectionHeader("Detalle") }
                items(state.rows.size) { index ->
                    val row = state.rows[index]
                    ProgressBarRow(
                        row.label.replaceFirstChar { it.titlecase() },
                        row.amount,
                        state.maxValue,
                        Formatters.formatNumber(row.amount),
                        row.color,
                        modifier = Modifier.padding(vertical = AgroSpacing.xxs),
                    )
                }
            }
        }
    }
}

