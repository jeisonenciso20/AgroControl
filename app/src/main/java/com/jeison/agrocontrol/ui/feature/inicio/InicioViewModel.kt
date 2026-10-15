package com.jeison.agrocontrol.ui.feature.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeison.agrocontrol.di.AppContainer
import com.jeison.agrocontrol.ui.common.Formatters
import com.jeison.agrocontrol.ui.components.KpiState
import com.jeison.agrocontrol.ui.common.PendingTaskUiMapper
import com.jeison.agrocontrol.ui.components.TaskItemUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class InicioUiState(
    val saludo: String = Formatters.greetingByHour(),
    val fechaHoy: String = Formatters.formatDateLong(),
    val animalesCount: String = "0",
    val animalesState: KpiState = KpiState.Loading,
    val lecheSemana: String = "0",
    val lecheState: KpiState = KpiState.Loading,
    val potrerosCount: String = "0",
    val potrerosState: KpiState = KpiState.Loading,
    val reproCount: String = "-",
    val reproState: KpiState = KpiState.Unavailable,
    val pendientes: List<TaskItemUi> = emptyList(),
    val pendientesTotal: Int = 0,
    val refreshing: Boolean = false,
    val lastSync: String? = null,
)

class InicioViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(InicioUiState())
    val state: StateFlow<InicioUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(refreshing = true) }
            container.syncStatus.syncNow()
            loadLocal()
            if (container.tokenStore.isLoggedIn()) {
                runCatching { container.api.dashboardSummary() }.onSuccess { summary ->
                    _state.update {
                        it.copy(
                            animalesCount = Formatters.formatInteger(summary.animalesRegistrados),
                            animalesState = KpiState.Value,
                            lecheSemana = Formatters.formatNumber(summary.litrosLecheSemana),
                            lecheState = KpiState.Value,
                            potrerosCount = Formatters.formatInteger(summary.potrerosRegistrados),
                            potrerosState = KpiState.Value,
                            reproCount = Formatters.formatInteger(summary.eventosReproduccion),
                            reproState = KpiState.Value,
                        )
                    }
                }
            }
            val lastPull = container.syncDao.getMetadata("last_pull")?.value
            _state.update { it.copy(refreshing = false, lastSync = lastPull) }
        }
    }

    private suspend fun loadLocal() {
        val animales = container.animalRepository.count()
        val potreros = container.localCacheDao.allPotreros().size
        val production = container.localCacheDao.allProduction()
        val weekAgo = LocalDate.now().minusDays(7)
        val leche = production.filter {
            it.productType.contains("leche", true) &&
                runCatching { LocalDate.parse(it.fecha.take(10)) }.getOrNull()?.let { d -> !d.isBefore(weekAgo) } == true
        }.sumOf { it.cantidad }
        val today = LocalDate.now()
        val potreroToday = PendingTaskUiMapper.potreroDueToday(container.localCacheDao.pendingTasks(), today)
        val taskUi = potreroToday.take(3).map { PendingTaskUiMapper.toTaskItemUi(it, today) }
        _state.update {
            it.copy(
                animalesCount = Formatters.formatInteger(animales),
                animalesState = KpiState.Value,
                lecheSemana = Formatters.formatNumber(leche),
                lecheState = if (leche > 0) KpiState.Value else it.lecheState,
                potrerosCount = Formatters.formatInteger(potreros),
                potrerosState = KpiState.Value,
                pendientes = taskUi,
                pendientesTotal = potreroToday.size,
            )
        }
    }

    fun completeTask(taskId: String) {
        viewModelScope.launch {
            container.pendingTaskRepository.completeTask(taskId)
            container.syncStatus.syncNow()
            loadLocal()
        }
    }
}
