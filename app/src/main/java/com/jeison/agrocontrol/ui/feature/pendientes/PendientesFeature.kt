package com.jeison.agrocontrol.ui.feature.pendientes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeison.agrocontrol.di.AppContainer
import com.jeison.agrocontrol.di.AppViewModelFactory
import com.jeison.agrocontrol.ui.common.PendingTaskUiMapper
import com.jeison.agrocontrol.ui.components.AgroScaffold
import com.jeison.agrocontrol.ui.components.EmptyState
import com.jeison.agrocontrol.ui.components.TaskItemUi
import com.jeison.agrocontrol.ui.components.TaskDetailDialog
import com.jeison.agrocontrol.ui.components.TaskListItem
import com.jeison.agrocontrol.ui.theme.AgroIcons
import com.jeison.agrocontrol.ui.theme.AgroSpacing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class PendientesUiState(val items: List<TaskItemUi> = emptyList(), val loading: Boolean = true)

class PendientesViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(PendientesUiState())
    val state: StateFlow<PendientesUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            container.syncStatus.syncNow()
            reloadLocal()
            _state.update { it.copy(loading = false) }
        }
    }

    fun completeTask(taskId: String) {
        viewModelScope.launch {
            container.pendingTaskRepository.completeTask(taskId)
            container.syncStatus.syncNow()
            reloadLocal()
        }
    }

    private suspend fun reloadLocal() {
        val today = LocalDate.now()
        val tasks = container.localCacheDao.pendingTasks().map { PendingTaskUiMapper.toTaskItemUi(it, today) }
        _state.update { it.copy(items = tasks) }
    }
}

@Composable
fun PendientesRoute(onBack: (() -> Unit)? = null, vm: PendientesViewModel = viewModel(factory = AppViewModelFactory)) {
    val state by vm.state.collectAsStateWithLifecycle()
    var selectedTask by remember { mutableStateOf<TaskItemUi?>(null) }
    selectedTask?.let { TaskDetailDialog(it) { selectedTask = null } }
    AgroScaffold(title = "Pendientes", onBack = onBack, isLoading = state.loading) { padding ->
        if (state.items.isEmpty()) {
            EmptyState(AgroIcons.Pendientes, "Sin pendientes", "Las tareas aparecerán al sincronizar", modifier = androidx.compose.ui.Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = androidx.compose.ui.Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(AgroSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AgroSpacing.xs),
            ) {
                items(state.items, key = { it.id }) { item ->
                    TaskListItem(
                        item,
                        onClick = { selectedTask = item },
                        showCompleteCheck = true,
                        onComplete = { vm.completeTask(item.id) },
                    )
                }
            }
        }
    }
}
