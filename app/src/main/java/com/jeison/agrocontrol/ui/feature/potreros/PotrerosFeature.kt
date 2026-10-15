package com.jeison.agrocontrol.ui.feature.potreros

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeison.agrocontrol.data.PendingTaskLocal
import com.jeison.agrocontrol.data.PotreroLocal
import com.jeison.agrocontrol.di.AppContainer
import com.jeison.agrocontrol.di.AppViewModelFactory
import com.jeison.agrocontrol.ui.common.Formatters
import com.jeison.agrocontrol.ui.common.PendingTaskUiMapper
import com.jeison.agrocontrol.ui.components.AgroCard
import com.jeison.agrocontrol.ui.components.AgroScaffold
import com.jeison.agrocontrol.ui.components.AgroTextField
import com.jeison.agrocontrol.ui.components.EmptyState
import com.jeison.agrocontrol.ui.components.LastSyncText
import com.jeison.agrocontrol.ui.components.SectionHeader
import com.jeison.agrocontrol.ui.components.SecondaryButton
import com.jeison.agrocontrol.ui.components.PrimaryButton
import com.jeison.agrocontrol.ui.components.StatRow
import com.jeison.agrocontrol.ui.components.StatusChip
import com.jeison.agrocontrol.ui.components.StatusChipTone
import com.jeison.agrocontrol.ui.components.TaskDetailDialog
import com.jeison.agrocontrol.ui.components.TaskListItem
import com.jeison.agrocontrol.ui.components.TaskItemUi
import com.jeison.agrocontrol.ui.theme.AgroIcons
import com.jeison.agrocontrol.ui.theme.AgroSpacing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class PotreroItemUi(
    val serverId: String,
    val nombre: String,
    val areaHectareas: Double?,
    val estado: String,
    val especies: List<String>,
    val animalCount: Int,
    val proximoTitulo: String?,
    val proximoVencimiento: String?,
)

data class PotrerosUiState(
    val items: List<PotreroItemUi> = emptyList(),
    val filtered: List<PotreroItemUi> = emptyList(),
    val estadoFilters: List<String> = emptyList(),
    val selectedEstado: String? = null,
    val query: String = "",
    val count: Int = 0,
    val totalHa: Double = 0.0,
    val loading: Boolean = true,
    val lastSync: String? = null,
)

class PotrerosViewModel(private val container: AppContainer) : ViewModel() {
    private val gson = Gson()
    private val _state = MutableStateFlow(PotrerosUiState())
    val state: StateFlow<PotrerosUiState> = _state.asStateFlow()

    init { load() }

    fun setQuery(q: String) {
        _state.update { it.copy(query = q) }
        applyFilters()
    }

    fun setEstado(estado: String?) {
        _state.update { it.copy(selectedEstado = estado) }
        applyFilters()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            container.syncStatus.syncNow()
            val lastPull = container.syncDao.getMetadata("last_pull")?.value
            val potreros = container.localCacheDao.allPotreros()
            val animalsByPotrero = container.animalRepository.getAll().groupBy { it.potrero }
            val pending = container.localCacheDao.pendingTasks().filter { it.status.equals("PENDING", true) }
            val mapped = potreros.map { p -> mapPotrero(p, animalsByPotrero, pending) }
            val estados = potreros.map { it.estado }.distinct().sortedBy { it.lowercase() }
            val totalHa = mapped.mapNotNull { it.areaHectareas }.sum()
            _state.update {
                it.copy(
                    items = mapped,
                    estadoFilters = estados,
                    count = mapped.size,
                    totalHa = totalHa,
                    lastSync = lastPull,
                    loading = false,
                )
            }
            applyFilters()
        }
    }

    private fun applyFilters() {
        val s = _state.value
        var list = s.items
        s.selectedEstado?.let { sel -> list = list.filter { it.estado.equals(sel, ignoreCase = true) } }
        if (s.query.isNotBlank()) {
            list = list.filter { it.nombre.contains(s.query, ignoreCase = true) }
        }
        _state.update { it.copy(filtered = list) }
    }

    private fun mapPotrero(
        p: PotreroLocal,
        animalsByPotrero: Map<String, List<com.jeison.agrocontrol.data.Animal>>,
        pending: List<PendingTaskLocal>,
    ): PotreroItemUi {
        val especies = parseEspecies(p.especiesJson)
        val count = animalsByPotrero[p.nombre]?.size ?: 0
        val task = pending.firstOrNull { it.referencia.equals(p.nombre, ignoreCase = true) }
        return PotreroItemUi(
            serverId = p.serverId,
            nombre = p.nombre,
            areaHectareas = p.areaHectareas,
            estado = p.estado,
            especies = especies,
            animalCount = count,
            proximoTitulo = task?.titulo,
            proximoVencimiento = task?.vencimiento?.let { Formatters.formatDate(it) },
        )
    }

    private fun parseEspecies(json: String): List<String> = runCatching {
        val type = object : TypeToken<List<String>>() {}.type
        gson.fromJson<List<String>>(json, type) ?: emptyList()
    }.getOrDefault(emptyList())
}

fun potreroEstadoTone(estado: String): StatusChipTone {
    val e = estado.lowercase()
    return when {
        e.contains("dispon") || e.contains("libre") -> StatusChipTone.SUCCESS
        e.contains("descans") -> StatusChipTone.WARNING
        e.contains("uso") || e.contains("ocup") -> StatusChipTone.INFO
        else -> StatusChipTone.NEUTRAL
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PotreroListItem(item: PotreroItemUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AgroCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(AgroIcons.Potrero, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(AgroSpacing.sm))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.nombre, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    StatusChip(item.estado, potreroEstadoTone(item.estado))
                }
                Text(
                    "${item.areaHectareas?.let { Formatters.formatNumber(it) } ?: "-"} ha",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (item.especies.isNotEmpty()) {
                    Text(
                        "Admite: ${item.especies.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                when {
                    item.proximoTitulo != null -> Text(
                        "Próximo: ${item.proximoTitulo}${item.proximoVencimiento?.let { " · $it" } ?: ""}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                    item.animalCount > 0 -> Text(
                        "${item.animalCount} animales",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PotrerosRoute(
    onDetail: (String) -> Unit,
    onAdd: () -> Unit = {},
    vm: PotrerosViewModel = viewModel(factory = AppViewModelFactory),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    AgroScaffold(
        title = "Potreros",
        isLoading = state.loading,
        fab = { com.jeison.agrocontrol.ui.components.AgroFabExtended("Nuevo potrero", onAdd) },
    ) { padding ->
        if (state.items.isEmpty() && !state.loading) {
            EmptyState(
                AgroIcons.Potrero,
                "Sin potreros",
                "Sincroniza para cargar los potreros de tu finca.",
                actionLabel = "Sincronizar ahora",
                onAction = vm::load,
                modifier = Modifier.padding(padding).fillMaxSize(),
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(bottom = AgroSpacing.lg),
            ) {
                item {
                    Text(
                        "${state.count} potreros · ${Formatters.formatNumber(state.totalHa)} ha en total",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = AgroSpacing.md, vertical = AgroSpacing.sm),
                    )
                    LastSyncText(state.lastSync, Modifier.padding(horizontal = AgroSpacing.md))
                    AgroTextField(
                        value = state.query,
                        onValueChange = vm::setQuery,
                        label = "Buscar potrero",
                        modifier = Modifier.padding(horizontal = AgroSpacing.md, vertical = AgroSpacing.sm),
                    )
                    FlowRow(
                        modifier = Modifier.padding(horizontal = AgroSpacing.md),
                        horizontalArrangement = Arrangement.spacedBy(AgroSpacing.xs),
                    ) {
                        FilterChip(
                            selected = state.selectedEstado == null,
                            onClick = { vm.setEstado(null) },
                            label = { Text("Todos") },
                        )
                        state.estadoFilters.forEach { est ->
                            FilterChip(
                                selected = state.selectedEstado.equals(est, ignoreCase = true),
                                onClick = { vm.setEstado(est) },
                                label = { Text(est.replaceFirstChar { c -> c.titlecase() }) },
                            )
                        }
                    }
                }
                if (state.filtered.isEmpty()) {
                    item {
                        Text(
                            "Ningún potrero coincide con el filtro",
                            modifier = Modifier.padding(AgroSpacing.md),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    items(state.filtered, key = { it.serverId }) { p ->
                        PotreroListItem(
                            p,
                            onClick = { onDetail(p.serverId) },
                            modifier = Modifier.padding(horizontal = AgroSpacing.md, vertical = AgroSpacing.xxs),
                        )
                    }
                }
            }
        }
    }
}

class PotreroDetalleViewModel(savedStateHandle: SavedStateHandle, private val container: AppContainer) : ViewModel() {
    private val serverId = savedStateHandle.get<String>("serverId").orEmpty()
    private val gson = Gson()
    private val _potrero = MutableStateFlow<PotreroLocal?>(null)
    val potrero: StateFlow<PotreroLocal?> = _potrero.asStateFlow()
    private val _animales = MutableStateFlow<List<String>>(emptyList())
    val animales: StateFlow<List<String>> = _animales.asStateFlow()
    private val _tasks = MutableStateFlow<List<TaskItemUi>>(emptyList())
    val tasks: StateFlow<List<TaskItemUi>> = _tasks.asStateFlow()

    init {
        viewModelScope.launch { load() }
    }

    fun load() {
        viewModelScope.launch {
            val p = container.localCacheDao.allPotreros().find { it.serverId == serverId }
            _potrero.value = p
            if (p != null) {
                _animales.value = container.animalRepository.getAll()
                    .filter { it.potrero == p.nombre }
                    .map { it.nombre.ifBlank { "${it.tipo} ${it.codigo}" } }
                reloadTasks(p.nombre)
            }
        }
    }

    fun completeTask(taskId: String) {
        viewModelScope.launch {
            container.pendingTaskRepository.completeTask(taskId)
            container.syncStatus.syncNow()
            _potrero.value?.let { reloadTasks(it.nombre) }
        }
    }

    private suspend fun reloadTasks(potreroNombre: String) {
        val today = LocalDate.now()
        _tasks.value = container.localCacheDao.pendingTasks()
            .filter { it.referencia.equals(potreroNombre, ignoreCase = true) && it.status.equals("PENDING", true) }
            .map { PendingTaskUiMapper.toTaskItemUi(it, today) }
    }

    fun especiesFor(p: PotreroLocal): List<String> = runCatching {
        val type = object : TypeToken<List<String>>() {}.type
        gson.fromJson<List<String>>(p.especiesJson, type) ?: emptyList()
    }.getOrDefault(emptyList())
}

private fun potreroEstadoLabel(estado: String): String =
    estado.replace('_', ' ').lowercase().replaceFirstChar { it.titlecase() }

@Composable
private fun PotreroMiniStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(AgroSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StatBlock(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = AgroSpacing.xs),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(AgroSpacing.sm))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = AgroSpacing.lg + AgroSpacing.sm, top = AgroSpacing.xxs),
        )
    }
}

@Composable
fun PotreroDetalleRoute(
    onBack: () -> Unit,
    onEdit: (String) -> Unit = {},
    onTrabajo: (String) -> Unit = {},
    vm: PotreroDetalleViewModel = viewModel(factory = AppViewModelFactory),
) {
    val potrero by vm.potrero.collectAsStateWithLifecycle()
    val animales by vm.animales.collectAsStateWithLifecycle()
    val tasks by vm.tasks.collectAsStateWithLifecycle()
    var selectedTask by remember { mutableStateOf<TaskItemUi?>(null) }
    selectedTask?.let { TaskDetailDialog(it) { selectedTask = null } }
    AgroScaffold(title = potrero?.nombre ?: "Potrero", onBack = onBack) { padding ->
        if (potrero == null) {
            EmptyState(AgroIcons.Potrero, "Potrero no encontrado", "", modifier = Modifier.padding(padding))
        } else {
            val p = potrero!!
            val especies = vm.especiesFor(p)
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(AgroSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AgroSpacing.sm),
            ) {
                item {
                    AgroCard(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(p.nombre, style = MaterialTheme.typography.titleLarge)
                            StatusChip(potreroEstadoLabel(p.estado), potreroEstadoTone(p.estado))
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            PotreroMiniStat(
                                "Área",
                                "${p.areaHectareas?.let { Formatters.formatNumber(it) } ?: "-"} ha",
                                Modifier.weight(1f),
                            )
                            PotreroMiniStat(
                                "Pasto",
                                p.tipoPasto.ifBlank { "—" },
                                Modifier.weight(1f),
                            )
                            PotreroMiniStat(
                                "Animales",
                                animales.size.toString(),
                                Modifier.weight(1f),
                            )
                        }
                    }
                }
                item { SectionHeader("Datos") }
                item {
                    AgroCard(Modifier.fillMaxWidth()) {
                        StatRow(AgroIcons.Pasto, "Estado", potreroEstadoLabel(p.estado))
                        if (p.fechaUltimaSiembra.isNotBlank()) {
                            StatRow(AgroIcons.Fecha, "Última siembra", Formatters.formatDate(p.fechaUltimaSiembra))
                        }
                        if (p.fechaProximoUso.isNotBlank()) {
                            StatRow(AgroIcons.Fecha, "Próximo uso", Formatters.formatDate(p.fechaProximoUso))
                        }
                        if (especies.isNotEmpty()) {
                            StatRow(AgroIcons.Bovino, "Especies", especies.joinToString(", "))
                        }
                        if (p.observacion.isNotBlank()) {
                            StatBlock(AgroIcons.Nota, "Observación", p.observacion)
                        }
                    }
                }
                item {
                    PrimaryButton("Editar potrero", { onEdit(p.serverId) }, modifier = Modifier.fillMaxWidth())
                    SecondaryButton(
                        "Registrar trabajo",
                        { onTrabajo(p.nombre) },
                        modifier = Modifier.fillMaxWidth().padding(top = AgroSpacing.xs),
                    )
                }
                item { SectionHeader("Animales en potrero") }
                if (animales.isEmpty()) {
                    item { Text("Ninguno", style = MaterialTheme.typography.bodyMedium) }
                } else {
                    items(animales) { name ->
                        Text("· $name", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = AgroSpacing.xxs))
                    }
                }
                item { SectionHeader("Trabajos pendientes") }
                if (tasks.isEmpty()) {
                    item { Text("Sin trabajos pendientes", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                } else {
                    items(tasks, key = { it.id }) { task ->
                        TaskListItem(
                            task,
                            onClick = { selectedTask = task },
                            modifier = Modifier.padding(vertical = AgroSpacing.xxs),
                            showCompleteCheck = true,
                            onComplete = { vm.completeTask(task.id) },
                        )
                    }
                }
            }
        }
    }
}

