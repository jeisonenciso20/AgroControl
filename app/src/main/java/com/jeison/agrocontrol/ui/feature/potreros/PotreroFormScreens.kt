package com.jeison.agrocontrol.ui.feature.potreros

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import com.jeison.agrocontrol.data.PendingTaskLocal
import com.jeison.agrocontrol.data.PotreroLocal
import com.jeison.agrocontrol.di.AppContainer
import com.jeison.agrocontrol.di.AppViewModelFactory
import com.jeison.agrocontrol.ui.components.AgroScaffold
import com.jeison.agrocontrol.ui.components.PrimaryButton
import com.jeison.agrocontrol.ui.feature.animales.DropdownField
import com.jeison.agrocontrol.ui.components.AgroDateField
import com.jeison.agrocontrol.ui.components.AgroField
import com.jeison.agrocontrol.ui.theme.AgroIcons
import com.jeison.agrocontrol.ui.theme.AgroSpacing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PotreroFormUiState(
    val nombre: String = "",
    val area: String = "",
    val tipoPasto: String = "",
    val estado: String = "DISPONIBLE",
    val fechaUltimaSiembra: String = "",
    val fechaProximoUso: String = "",
    val observacion: String = "",
    val isEdit: Boolean = false,
    val serverId: String = "",
    val error: String? = null,
    val saved: Boolean = false,
    val loading: Boolean = false,
)

class PotreroFormViewModel(savedStateHandle: SavedStateHandle, private val container: AppContainer) : ViewModel() {
    private val editId = savedStateHandle.get<String>("serverId")
    private val _state = MutableStateFlow(PotreroFormUiState(isEdit = editId != null))
    val state: StateFlow<PotreroFormUiState> = _state.asStateFlow()

    init {
        editId?.let { id ->
            viewModelScope.launch {
                container.localCacheDao.allPotreros().find { it.serverId == id }?.let { p ->
                    _state.update {
                        it.copy(
                            serverId = p.serverId,
                            nombre = p.nombre,
                            area = p.areaHectareas?.toString().orEmpty(),
                            tipoPasto = p.tipoPasto,
                            estado = p.estado,
                            fechaUltimaSiembra = p.fechaUltimaSiembra,
                            fechaProximoUso = p.fechaProximoUso,
                            observacion = p.observacion,
                        )
                    }
                }
            }
        }
    }

    fun update(block: PotreroFormUiState.() -> PotreroFormUiState) = _state.update(block)

    fun save() {
        val s = _state.value
        val area = s.area.replace(",", ".").toDoubleOrNull()
        when {
            s.nombre.isBlank() -> _state.update { it.copy(error = "Nombre obligatorio") }
            area == null || area <= 0 -> _state.update { it.copy(error = "Área inválida") }
            else -> viewModelScope.launch {
                _state.update { it.copy(loading = true, error = null) }
                val row = PotreroLocal(
                    serverId = s.serverId,
                    nombre = s.nombre.trim(),
                    areaHectareas = area,
                    estado = s.estado,
                    tipoPasto = s.tipoPasto,
                    fechaUltimaSiembra = s.fechaUltimaSiembra,
                    fechaProximoUso = s.fechaProximoUso,
                    observacion = s.observacion,
                    especiesJson = Gson().toJson(emptyList<String>()),
                    updatedAtEpoch = System.currentTimeMillis(),
                )
                runCatching {
                    if (s.isEdit) container.potreroRepository.update(row)
                    else container.potreroRepository.insert(row)
                }.onSuccess { _state.update { it.copy(loading = false, saved = true) } }
                    .onFailure { e -> _state.update { it.copy(loading = false, error = e.message) } }
            }
        }
    }

    fun clearSaved() = _state.update { it.copy(saved = false) }
}

data class PotreroTrabajoUiState(
    val potrero: String = "",
    val workType: String = "FUMIGAR",
    val otroTexto: String = "",
    val vencimiento: String = "",
    val observacion: String = "",
    val potreroOptions: List<String> = emptyList(),
    val error: String? = null,
    val saved: Boolean = false,
)

class PotreroTrabajoViewModel(savedStateHandle: SavedStateHandle, private val container: AppContainer) : ViewModel() {
    private val prePotrero = savedStateHandle.get<String>("potrero").orEmpty()
    private val _state = MutableStateFlow(PotreroTrabajoUiState(potrero = prePotrero))
    val state: StateFlow<PotreroTrabajoUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val names = container.localCacheDao.allPotreros().map { it.nombre }
            _state.update { it.copy(potreroOptions = names, potrero = prePotrero.ifBlank { names.firstOrNull().orEmpty() }) }
        }
    }

    fun update(block: PotreroTrabajoUiState.() -> PotreroTrabajoUiState) = _state.update(block)

    fun save() {
        val s = _state.value
        val work = if (s.workType == "OTRO") s.otroTexto else s.workType
        when {
            s.potrero.isBlank() -> _state.update { it.copy(error = "Selecciona potrero") }
            s.vencimiento.isBlank() -> _state.update { it.copy(error = "Fecha programada obligatoria") }
            work.isBlank() -> _state.update { it.copy(error = "Tipo de trabajo obligatorio") }
            else -> viewModelScope.launch {
                val titulo = "$work ${s.potrero}"
                container.pendingTaskRepository.insert(
                    PendingTaskLocal(
                        serverId = "",
                        titulo = titulo,
                        tipo = "POTRERO",
                        status = "PENDING",
                        vencimiento = s.vencimiento,
                        referencia = s.potrero,
                        workType = work,
                        observacion = s.observacion,
                        updatedAtEpoch = System.currentTimeMillis(),
                    ),
                )
                _state.update { it.copy(saved = true) }
            }
        }
    }

    fun clearSaved() = _state.update { it.copy(saved = false) }
}

@Composable
fun PotreroFormRoute(onBack: () -> Unit, onSaved: () -> Unit, vm: PotreroFormViewModel = viewModel(factory = AppViewModelFactory)) {
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) { if (state.saved) { vm.clearSaved(); onSaved() } }
    val pastos = listOf("Brachiaria", "Estrella", "Kikuyo", "Guinea", "Angleton", "Pangola", "Otro")
    val estados = listOf("DISPONIBLE", "RECUPERACION", "NO_DISPONIBLE")
    AgroScaffold(title = if (state.isEdit) "Editar potrero" else "Nuevo potrero", onBack = onBack) { padding ->
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(AgroSpacing.md)) {
            AgroField(state.nombre, { vm.update { copy(nombre = it) } }, "Nombre", leadingIcon = AgroIcons.Potrero)
            Spacer(Modifier.height(AgroSpacing.sm))
            AgroField(state.area, { vm.update { copy(area = it) } }, "Área (hectáreas)", leadingIcon = AgroIcons.Area)
            Spacer(Modifier.height(AgroSpacing.sm))
            DropdownField("Tipo de pasto", state.tipoPasto, pastos) { vm.update { copy(tipoPasto = it) } }
            Spacer(Modifier.height(AgroSpacing.sm))
            DropdownField("Estado", state.estado, estados) { vm.update { copy(estado = it) } }
            Spacer(Modifier.height(AgroSpacing.sm))
            AgroDateField(state.fechaUltimaSiembra, { vm.update { copy(fechaUltimaSiembra = it) } }, "Última siembra")
            Spacer(Modifier.height(AgroSpacing.sm))
            AgroDateField(state.fechaProximoUso, { vm.update { copy(fechaProximoUso = it) } }, "Próximo uso")
            Spacer(Modifier.height(AgroSpacing.sm))
            AgroField(state.observacion, { vm.update { copy(observacion = it) } }, "Observaciones", leadingIcon = AgroIcons.Nota, singleLine = false)
            state.error?.let { androidx.compose.material3.Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(AgroSpacing.lg))
            PrimaryButton("Guardar potrero", vm::save, enabled = !state.loading)
        }
    }
}

@Composable
fun PotreroTrabajoRoute(onBack: () -> Unit, onSaved: () -> Unit, vm: PotreroTrabajoViewModel = viewModel(factory = AppViewModelFactory)) {
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) { if (state.saved) { vm.clearSaved(); onSaved() } }
    val works = listOf("FUMIGAR", "ABONAR", "LIMPIAR", "REVISAR_PASTO", "OTRO")
    AgroScaffold(title = "Registrar trabajo", onBack = onBack) { padding ->
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(AgroSpacing.md)) {
            DropdownField("Potrero", state.potrero, state.potreroOptions) { vm.update { copy(potrero = it) } }
            Spacer(Modifier.height(AgroSpacing.sm))
            DropdownField("Tipo de trabajo", state.workType, works) { vm.update { copy(workType = it) } }
            if (state.workType == "OTRO") {
                Spacer(Modifier.height(AgroSpacing.sm))
                AgroField(state.otroTexto, { vm.update { copy(otroTexto = it) } }, "¿Cuál?", modifier = Modifier.fillMaxWidth())
            }
            Spacer(Modifier.height(AgroSpacing.sm))
            AgroDateField(state.vencimiento, { vm.update { copy(vencimiento = it) } }, "Fecha programada")
            Spacer(Modifier.height(AgroSpacing.sm))
            AgroField(state.observacion, { vm.update { copy(observacion = it) } }, "Observaciones", singleLine = false)
            state.error?.let { androidx.compose.material3.Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(AgroSpacing.lg))
            PrimaryButton("Guardar trabajo", vm::save)
        }
    }
}
