package com.jeison.agrocontrol.ui.feature.produccion

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
import com.jeison.agrocontrol.data.ProductionLocal
import com.jeison.agrocontrol.di.AppContainer
import com.jeison.agrocontrol.di.AppViewModelFactory
import com.jeison.agrocontrol.ui.components.AgroDateField
import com.jeison.agrocontrol.ui.components.AgroField
import com.jeison.agrocontrol.ui.common.productionUnitFor
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

data class ProduccionRegistrarUiState(
    val animalLabel: String = "",
    val animalServerId: String = "",
    val animalCodigo: String = "",
    val fecha: String = LocalDate.now().toString(),
    val jornada: String = if (java.time.LocalTime.now().hour < 12) "MANANA" else "TARDE",
    val cantidad: String = "",
    val productType: String = "LECHE",
    val notas: String = "",
    val animalOptions: List<Pair<String, String>> = emptyList(),
    val error: String? = null,
    val saved: Boolean = false,
)

class ProduccionRegistrarViewModel(savedStateHandle: SavedStateHandle, private val container: AppContainer) : ViewModel() {
    private val preCodigo = savedStateHandle.get<String>("codigo")
    private val _state = MutableStateFlow(ProduccionRegistrarUiState())
    val state: StateFlow<ProduccionRegistrarUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val hembras = container.animalRepository.getAll()
                .filter { it.sexo.equals("Hembra", true) || it.tipo.equals("Vaca", true) }
            val options = hembras.map { a ->
                animalDisplayTitle(a) to "${a.serverId.orEmpty()}|${a.codigo}"
            }
            val pre = preCodigo?.let { c -> hembras.find { it.codigo == c } }
            _state.update {
                it.copy(
                    animalOptions = options.map { o -> o.first to o.second },
                    animalLabel = pre?.let { p -> animalDisplayTitle(p) } ?: options.firstOrNull()?.first.orEmpty(),
                    animalServerId = pre?.serverId.orEmpty(),
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

    fun update(block: ProduccionRegistrarUiState.() -> ProduccionRegistrarUiState) = _state.update(block)

    fun save() {
        val s = _state.value
        val qty = s.cantidad.replace(",", ".").toDoubleOrNull()
        when {
            s.animalServerId.isBlank() && s.animalCodigo.isBlank() -> _state.update { it.copy(error = "Selecciona animal") }
            qty == null || qty <= 0 -> _state.update { it.copy(error = "Cantidad inválida") }
            s.fecha.isBlank() -> _state.update { it.copy(error = "Fecha obligatoria") }
            else -> viewModelScope.launch {
                container.productionRepository.insert(
                    ProductionLocal(
                        serverId = "",
                        productType = s.productType,
                        cantidad = qty,
                        unidad = productionUnitFor(s.productType),
                        fecha = s.fecha,
                        jornada = s.jornada,
                        animalServerId = s.animalServerId,
                        animalCodigo = s.animalCodigo,
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
fun ProduccionRegistrarRoute(onBack: () -> Unit, onSaved: () -> Unit, vm: ProduccionRegistrarViewModel = viewModel(factory = AppViewModelFactory)) {
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) { if (state.saved) { vm.clearSaved(); onSaved() } }
    val jornadas = listOf("MANANA" to "Mañana", "TARDE" to "Tarde")
    val tipos = listOf("LECHE", "CARNE", "HUEVO", "OTRO")
    AgroScaffold(title = "Registrar producción", onBack = onBack) { padding ->
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(AgroSpacing.md)) {
            val labels = state.animalOptions.map { it.first }
            val values = state.animalOptions.map { it.second }
            DropdownField("Animal", state.animalLabel, labels) { label ->
                val idx = labels.indexOf(label)
                if (idx >= 0) vm.selectAnimal(values[idx], label)
            }
            Spacer(Modifier.height(AgroSpacing.sm))
            AgroDateField(state.fecha, { vm.update { copy(fecha = it) } }, "Fecha")
            Spacer(Modifier.height(AgroSpacing.sm))
            DropdownField("Jornada", jornadas.first { it.first == state.jornada }.second, jornadas.map { it.second }) { sel ->
                val code = jornadas.first { it.second == sel }.first
                vm.update { copy(jornada = code) }
            }
            Spacer(Modifier.height(AgroSpacing.sm))
            val unidad = productionUnitFor(state.productType)
            AgroField(
                state.cantidad,
                { vm.update { copy(cantidad = it) } },
                "Cantidad ($unidad)",
                leadingIcon = AgroIcons.Leche,
            )
            Spacer(Modifier.height(AgroSpacing.sm))
            DropdownField("Tipo producción", state.productType, tipos) { vm.update { copy(productType = it) } }
            Spacer(Modifier.height(AgroSpacing.sm))
            AgroField(state.notas, { vm.update { copy(notas = it) } }, "Observaciones", singleLine = false)
            state.error?.let { androidx.compose.material3.Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(AgroSpacing.lg))
            PrimaryButton("Guardar", vm::save)
        }
    }
}
