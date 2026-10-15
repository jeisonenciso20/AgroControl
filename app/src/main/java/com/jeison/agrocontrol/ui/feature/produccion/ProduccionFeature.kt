package com.jeison.agrocontrol.ui.feature.produccion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeison.agrocontrol.data.ProductionLocal
import com.jeison.agrocontrol.di.AppContainer
import com.jeison.agrocontrol.di.AppViewModelFactory
import com.jeison.agrocontrol.ui.common.Formatters
import com.jeison.agrocontrol.ui.components.AgroCard
import com.jeison.agrocontrol.ui.components.AgroScaffold
import com.jeison.agrocontrol.ui.components.EmptyState
import com.jeison.agrocontrol.ui.components.KpiAccent
import com.jeison.agrocontrol.ui.components.KpiTile
import com.jeison.agrocontrol.ui.components.LastSyncText
import com.jeison.agrocontrol.ui.components.SectionHeader
import com.jeison.agrocontrol.ui.theme.AgroIcons
import com.jeison.agrocontrol.ui.theme.AgroSpacing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ProductionTotalUi(
    val productType: String,
    val unidad: String,
    val total: Double,
)

data class ProductionEntryUi(
    val productType: String,
    val cantidad: Double,
    val unidad: String,
    val notas: String,
)

data class ProductionDayGroup(
    val fecha: LocalDate,
    val dayLabel: String,
    val items: List<ProductionEntryUi>,
)

data class ProduccionUiState(
    val periodDays: Int? = 7,
    val productFilter: String? = null,
    val productTypes: List<String> = emptyList(),
    val totals: List<ProductionTotalUi> = emptyList(),
    val groups: List<ProductionDayGroup> = emptyList(),
    val lastSync: String? = null,
    val loading: Boolean = true,
    val refreshing: Boolean = false,
)

class ProduccionViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(ProduccionUiState())
    val state: StateFlow<ProduccionUiState> = _state.asStateFlow()

    init { load() }

    fun setPeriodDays(days: Int?) {
        _state.update { it.copy(periodDays = days) }
        rebuildFromCache()
    }

    fun setProductFilter(filter: String?) {
        _state.update { it.copy(productFilter = filter) }
        rebuildFromCache()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            container.syncStatus.syncNow()
            val lastPull = container.syncDao.getMetadata("last_pull")?.value
            val all = container.localCacheDao.allProduction()
            val types = all.map { it.productType }.distinct().sortedBy { it.lowercase() }
            _state.update { it.copy(lastSync = lastPull, productTypes = types, loading = false) }
            rebuildFromCache(all)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(refreshing = true) }
            container.syncStatus.syncNow()
            val lastPull = container.syncDao.getMetadata("last_pull")?.value
            val all = container.localCacheDao.allProduction()
            val types = all.map { it.productType }.distinct().sortedBy { it.lowercase() }
            _state.update { it.copy(lastSync = lastPull, productTypes = types, refreshing = false) }
            rebuildFromCache(all)
        }
    }

    private fun rebuildFromCache() {
        viewModelScope.launch {
            rebuildFromCache(container.localCacheDao.allProduction())
        }
    }

    private fun rebuildFromCache(all: List<ProductionLocal>) {
        val s = _state.value
        val today = LocalDate.now()
        val filtered = all.filter { record ->
            val date = parseDate(record.fecha) ?: return@filter false
            val inPeriod = when (s.periodDays) {
                null -> true
                else -> !date.isBefore(today.minusDays(s.periodDays!!.toLong() - 1))
            }
            val matchesProduct = s.productFilter == null ||
                record.productType.equals(s.productFilter, ignoreCase = true)
            inPeriod && matchesProduct
        }
        val totals = filtered
            .groupBy { it.productType to it.unidad }
            .map { (key, rows) ->
                ProductionTotalUi(key.first, key.second, rows.sumOf { it.cantidad })
            }
            .sortedByDescending { it.total }
        val groups = filtered
            .groupBy { parseDate(it.fecha) ?: LocalDate.MIN }
            .filterKeys { it != LocalDate.MIN }
            .toList()
            .sortedByDescending { it.first }
            .map { (date, rows) ->
                ProductionDayGroup(
                    fecha = date,
                    dayLabel = dayLabel(date, today),
                    items = rows.map {
                        ProductionEntryUi(it.productType, it.cantidad, it.unidad, it.notas)
                    }.sortedByDescending { it.cantidad },
                )
            }
        _state.update { it.copy(totals = totals, groups = groups) }
    }

    private fun parseDate(fecha: String): LocalDate? =
        runCatching { LocalDate.parse(fecha.take(10)) }.getOrNull()

    private fun dayLabel(date: LocalDate, today: LocalDate): String = when (date) {
        today -> "Hoy"
        today.minusDays(1) -> "Ayer"
        else -> Formatters.formatDate(date.toString())
    }
}

private fun productionIcon(productType: String): ImageVector = when {
    productType.contains("leche", true) -> AgroIcons.Leche
    productType.contains("carne", true) -> AgroIcons.Peso
    else -> AgroIcons.Produccion
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProduccionRoute(
    onRegistrar: () -> Unit = {},
    vm: ProduccionViewModel = viewModel(factory = AppViewModelFactory),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    AgroScaffold(
        title = "Producción",
        isLoading = state.loading && !state.refreshing,
        fab = { com.jeison.agrocontrol.ui.components.AgroFabExtended("Registrar", onRegistrar) },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.refreshing,
            onRefresh = vm::refresh,
            modifier = Modifier.padding(padding).fillMaxSize(),
        ) {
            if (state.groups.isEmpty() && !state.loading) {
                EmptyState(
                    AgroIcons.Produccion,
                    "Sin registros",
                    "Sincroniza para ver la producción de tu finca.",
                    actionLabel = "Sincronizar ahora",
                    onAction = vm::refresh,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = AgroSpacing.lg),
                ) {
                    item {
                        LastSyncText(state.lastSync, Modifier.padding(horizontal = AgroSpacing.md, vertical = AgroSpacing.xs))
                        Text(
                            "Periodo y rubro",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = AgroSpacing.md, vertical = AgroSpacing.xxs),
                        )
                        FlowRow(
                            modifier = Modifier.padding(horizontal = AgroSpacing.md),
                            horizontalArrangement = Arrangement.spacedBy(AgroSpacing.xs),
                        ) {
                            listOf(7 to "7 días", 30 to "30 días", null to "Todo").forEach { (days, label) ->
                                FilterChip(
                                    selected = state.periodDays == days,
                                    onClick = { vm.setPeriodDays(days) },
                                    label = { Text(label) },
                                )
                            }
                        }
                        FlowRow(
                            modifier = Modifier.padding(horizontal = AgroSpacing.md, vertical = AgroSpacing.xxs),
                            horizontalArrangement = Arrangement.spacedBy(AgroSpacing.xs),
                        ) {
                            FilterChip(
                                selected = state.productFilter == null,
                                onClick = { vm.setProductFilter(null) },
                                label = { Text("Todos") },
                            )
                            state.productTypes.forEach { type ->
                                FilterChip(
                                    selected = state.productFilter.equals(type, ignoreCase = true),
                                    onClick = { vm.setProductFilter(type) },
                                    label = { Text(type.replaceFirstChar { c -> c.titlecase() }) },
                                )
                            }
                        }
                    }
                    if (state.totals.isNotEmpty()) {
                        item {
                            SectionHeader("Totales")
                            Column(
                                modifier = Modifier.padding(horizontal = AgroSpacing.md),
                                verticalArrangement = Arrangement.spacedBy(AgroSpacing.sm),
                            ) {
                                state.totals.take(4).chunked(2).forEach { row ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(AgroSpacing.sm)) {
                                        row.forEach { total ->
                                            KpiTile(
                                                icon = productionIcon(total.productType),
                                                label = total.productType.replaceFirstChar { it.titlecase() },
                                                value = Formatters.formatNumber(total.total),
                                                unit = total.unidad,
                                                accent = if (total.productType.contains("leche", true)) KpiAccent.TERTIARY else KpiAccent.PRIMARY,
                                                modifier = Modifier.weight(1f),
                                            )
                                        }
                                        if (row.size == 1) Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                    state.groups.forEach { group ->
                        item(key = "header-${group.fecha}") {
                            SectionHeader(group.dayLabel)
                        }
                        items(group.items, key = { "${group.fecha}-${it.productType}-${it.cantidad}" }) { entry ->
                            ProductionRow(entry, modifier = Modifier.padding(horizontal = AgroSpacing.md, vertical = AgroSpacing.xxs))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductionRow(entry: ProductionEntryUi, modifier: Modifier = Modifier) {
    AgroCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(productionIcon(entry.productType), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(AgroSpacing.sm))
            Column(Modifier.weight(1f)) {
                Text(entry.productType.replaceFirstChar { it.titlecase() }, style = MaterialTheme.typography.titleSmall)
                if (entry.notas.isNotBlank()) {
                    Text(entry.notas, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(
                "${Formatters.formatNumber(entry.cantidad)} ${entry.unidad}",
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

