package com.jeison.agrocontrol.ui.feature.reportes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeison.agrocontrol.di.AppContainer
import com.jeison.agrocontrol.di.AppViewModelFactory
import com.jeison.agrocontrol.ui.common.Formatters
import com.jeison.agrocontrol.ui.components.AgroScaffold
import com.jeison.agrocontrol.ui.components.KpiAccent
import com.jeison.agrocontrol.ui.components.KpiTile
import com.jeison.agrocontrol.ui.components.ProgressBarRow
import com.jeison.agrocontrol.ui.theme.AgroIcons
import com.jeison.agrocontrol.ui.theme.AgroSpacing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

enum class ReportMode { WEEKLY, MONTHLY }

data class RubroRow(val name: String, val total: Double, val unitSuffix: String)

data class ReportUi(
    val periodLabel: String,
    val recordCount: Int,
    val rows: List<RubroRow>,
)

data class ReportesUiState(
    val mode: ReportMode = ReportMode.WEEKLY,
    val weekStart: LocalDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)),
    val month: YearMonth = YearMonth.now(),
    val report: ReportUi? = null,
    val loading: Boolean = false,
    val errorMessage: String? = null,
)

class ReportesViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(ReportesUiState())
    val state: StateFlow<ReportesUiState> = _state.asStateFlow()

    init { loadCurrent() }

    fun setMode(mode: ReportMode) {
        _state.update { it.copy(mode = mode, errorMessage = null) }
        loadCurrent()
    }

    fun previousPeriod() {
        _state.update {
            when (it.mode) {
                ReportMode.WEEKLY -> it.copy(weekStart = it.weekStart.minusWeeks(1))
                ReportMode.MONTHLY -> it.copy(month = it.month.minusMonths(1))
            }
        }
        loadCurrent()
    }

    fun nextPeriod() {
        val s = _state.value
        if (!canGoNext(s)) return
        _state.update {
            when (it.mode) {
                ReportMode.WEEKLY -> it.copy(weekStart = it.weekStart.plusWeeks(1))
                ReportMode.MONTHLY -> it.copy(month = it.month.plusMonths(1))
            }
        }
        loadCurrent()
    }

    fun canGoNext(state: ReportesUiState = _state.value): Boolean = when (state.mode) {
        ReportMode.WEEKLY -> state.weekStart.plusDays(6).isBefore(LocalDate.now())
        ReportMode.MONTHLY -> state.month.isBefore(YearMonth.now())
    }

    fun loadCurrent() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, errorMessage = null) }
            if (!container.connectivity.isOnline.first()) {
                _state.update {
                    it.copy(
                        loading = false,
                        errorMessage = "Los reportes necesitan internet. Conéctate e inténtalo de nuevo.",
                    )
                }
                return@launch
            }
            val s = _state.value
            runCatching {
                when (s.mode) {
                    ReportMode.WEEKLY -> {
                        val from = s.weekStart
                        val to = s.weekStart.plusDays(6)
                        container.api.reportWeekly(from.toString(), to.toString()) to formatWeekLabel(from, to)
                    }
                    ReportMode.MONTHLY -> {
                        container.api.reportMonthly(s.month.year, s.month.monthValue) to formatMonthLabel(s.month)
                    }
                }
            }.onSuccess { (response, label) ->
                val rows = response.totalsByProductType.entries
                    .map { (name, total) ->
                        val suffix = if (name.contains("leche", true)) " litros" else ""
                        RubroRow(name.replaceFirstChar { c -> c.titlecase() }, total, suffix)
                    }
                    .sortedByDescending { it.total }
                _state.update {
                    it.copy(
                        loading = false,
                        report = ReportUi(label, response.recordCount, rows),
                        errorMessage = null,
                    )
                }
            }.onFailure {
                _state.update {
                    it.copy(
                        loading = false,
                        errorMessage = "No pudimos cargar el reporte. Revisa tu conexión e inténtalo otra vez.",
                    )
                }
            }
        }
    }

    private fun formatWeekLabel(from: LocalDate, to: LocalDate): String {
        val locale = Locale("es", "CO")
        val dayMonth = DateTimeFormatter.ofPattern("d MMM", locale)
        val year = DateTimeFormatter.ofPattern("yyyy", locale)
        return "${from.format(dayMonth)} – ${to.format(dayMonth)} ${to.format(year)}"
    }

    private fun formatMonthLabel(month: YearMonth): String {
        val locale = Locale("es", "CO")
        val fmt = DateTimeFormatter.ofPattern("MMMM yyyy", locale)
        return month.atDay(1).format(fmt).replaceFirstChar { c -> c.titlecase(locale) }
    }

    fun periodLabelFor(state: ReportesUiState): String = when (state.mode) {
        ReportMode.WEEKLY -> formatWeekLabel(state.weekStart, state.weekStart.plusDays(6))
        ReportMode.MONTHLY -> formatMonthLabel(state.month)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesRoute(onBack: (() -> Unit)? = null, vm: ReportesViewModel = viewModel(factory = AppViewModelFactory)) {
    val state by vm.state.collectAsStateWithLifecycle()
    AgroScaffold(title = "Reportes", onBack = onBack, isLoading = state.loading) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AgroSpacing.md),
        ) {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = state.mode == ReportMode.WEEKLY,
                    onClick = { vm.setMode(ReportMode.WEEKLY) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                ) { Text("Semanal") }
                SegmentedButton(
                    selected = state.mode == ReportMode.MONTHLY,
                    onClick = { vm.setMode(ReportMode.MONTHLY) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                ) { Text("Mensual") }
            }
            Row(
                Modifier.fillMaxWidth().padding(vertical = AgroSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                IconButton(onClick = vm::previousPeriod) {
                    Icon(Icons.Rounded.ChevronLeft, contentDescription = "Periodo anterior", modifier = Modifier.size(48.dp))
                }
                Text(
                    state.report?.periodLabel ?: vm.periodLabelFor(state),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                IconButton(onClick = vm::nextPeriod, enabled = vm.canGoNext()) {
                    Icon(Icons.Rounded.ChevronRight, contentDescription = "Periodo siguiente", modifier = Modifier.size(48.dp))
                }
            }
            state.errorMessage?.let { msg ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().padding(bottom = AgroSpacing.md),
                ) {
                    Text(msg, modifier = Modifier.padding(AgroSpacing.md), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
            state.report?.let { report ->
                KpiTile(
                    icon = AgroIcons.Produccion,
                    label = "Registros",
                    value = Formatters.formatInteger(report.recordCount),
                    caption = "en el periodo",
                    accent = KpiAccent.PRIMARY,
                    modifier = Modifier.padding(bottom = AgroSpacing.md),
                )
                if (report.rows.isEmpty()) {
                    Text("Sin datos en este periodo", style = MaterialTheme.typography.bodyMedium)
                } else {
                    val max = report.rows.maxOf { it.total }.coerceAtLeast(1.0)
                    report.rows.forEach { row ->
                        ProgressBarRow(
                            label = row.name,
                            value = row.total,
                            max = max,
                            valueText = Formatters.formatNumber(row.total) + row.unitSuffix,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

