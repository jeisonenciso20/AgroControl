package com.jeison.agrocontrol.ui.feature.reproduccion

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import com.jeison.agrocontrol.data.ReproductionEventLocal
import com.jeison.agrocontrol.di.AppContainer
import com.jeison.agrocontrol.di.AppViewModelFactory
import com.jeison.agrocontrol.ui.components.AgroDateField
import com.jeison.agrocontrol.ui.components.AgroField
import com.jeison.agrocontrol.ui.components.AgroScaffold
import com.jeison.agrocontrol.ui.components.PrimaryButton
import com.jeison.agrocontrol.ui.feature.animales.DropdownField
import com.jeison.agrocontrol.ui.feature.animales.animalDisplayTitle
import com.jeison.agrocontrol.ui.theme.AgroIcons
import com.jeison.agrocontrol.ui.theme.AgroSpacing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ReproFormUiState(
    val animalLabel: String = "",
    val animalServerId: String = "",
    val animalCodigo: String = "",
    val tipoEvento: String = "INSEMINACION",
    val programada: String = LocalDate.now().toString(),
    val notas: String = "",
    val animalOptions: List<Pair<String, String>> = emptyList(),
    val error: String? = null,
    val saved: Boolean = false,
)

class ReproduccionFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val container: AppContainer,
) : ViewModel() {
    private val preCodigo = savedStateHandle.get<String>("codigo").orEmpty()
    private val _state = MutableStateFlow(ReproFormUiState())
    val state: StateFlow<ReproFormUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val hembras = container.animalRepository.getAll().filter { it.sexo.equals("Hembra", true) }
            val options = hembras.map { a ->
                animalDisplayTitle(a) to "${a.serverId.orEmpty()}|${a.codigo}"
            }
            val pre = preCodigo.takeIf { it.isNotBlank() }?.let { c -> hembras.find { it.codigo == c } }
            _state.update {
                it.copy(
                    animalOptions = options,
                    animalLabel = pre?.let { p -> animalDisplayTitle(p) } ?: options.firstOrNull()?.first.orEmpty(),
                    animalServerId = pre?.serverId.orEmpty().ifBlank { options.firstOrNull()?.second?.substringBefore("|").orEmpty() },
                    animalCodigo = pre?.codigo ?: options.firstOrNull()?.second?.substringAfter("|").orEmpty(),
                )
            }
        }
    }

    fun selectAnimal(optionValue: String, label: String) {
        val parts = optionValue.split("|")
        _state.update {
            it.copy(
                animalLabel = label,
                animalServerId = parts.getOrElse(0) { "" },
                animalCodigo = parts.getOrElse(1) { "" },
            )
        }
    }

    fun update(block: ReproFormUiState.() -> ReproFormUiState) = _state.update(block)

    fun save() {
        val s = _state.value
        when {
            s.animalServerId.isBlank() -> _state.update { it.copy(error = "Selecciona hembra") }
            s.programada.isBlank() -> _state.update { it.copy(error = "Fecha obligatoria") }
            else -> viewModelScope.launch {
                container.reproductionRepository.insert(
                    ReproductionEventLocal(
                        serverId = "",
                        animalServerId = s.animalServerId,
                        animalCodigo = s.animalCodigo,
                        tipoEvento = s.tipoEvento,
                        programada = s.programada,
                        notas = s.notas,
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
fun ReproduccionRegistrarRoute(onBack: () -> Unit, onSaved: () -> Unit, vm: ReproduccionFormViewModel = viewModel(factory = AppViewModelFactory)) {
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) { if (state.saved) { vm.clearSaved(); onSaved() } }
    val tipos = listOf("CELO", "INSEMINACION", "DIAGNOSTICO", "PARTO")
    AgroScaffold(title = "Registrar evento", onBack = onBack) { padding ->
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(AgroSpacing.md)) {
            val labels = state.animalOptions.map { it.first }
            val values = state.animalOptions.map { it.second }
            DropdownField("Animal", state.animalLabel, labels) { label ->
                val idx = labels.indexOf(label)
                if (idx >= 0) vm.selectAnimal(values[idx], label)
            }
            Spacer(Modifier.height(AgroSpacing.sm))
            DropdownField("Tipo evento", state.tipoEvento, tipos) { vm.update { copy(tipoEvento = it) } }
            Spacer(Modifier.height(AgroSpacing.sm))
            AgroDateField(state.programada, { vm.update { copy(programada = it) } }, "Fecha programada")
            Spacer(Modifier.height(AgroSpacing.sm))
            AgroField(state.notas, { vm.update { copy(notas = it) } }, "Observaciones", singleLine = false)
            state.error?.let { androidx.compose.material3.Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(AgroSpacing.lg))
            PrimaryButton("Guardar evento", vm::save)
        }
    }
}
