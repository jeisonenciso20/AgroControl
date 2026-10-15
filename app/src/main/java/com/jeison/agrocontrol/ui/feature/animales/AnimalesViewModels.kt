package com.jeison.agrocontrol.ui.feature.animales

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeison.agrocontrol.data.Animal
import com.jeison.agrocontrol.data.ProductionLocal
import com.jeison.agrocontrol.data.ReproductionEventLocal
import com.jeison.agrocontrol.di.AppContainer
import com.jeison.agrocontrol.ui.common.Formatters
import com.jeison.agrocontrol.ui.components.AnimalItemUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

fun animalDisplayTitle(animal: Animal): String =
    animal.nombre.ifBlank { "${animal.tipo} ${animal.codigo}" }

data class AnimalesListaUiState(
    val query: String = "",
    val items: List<AnimalItemUi> = emptyList(),
    val loading: Boolean = true,
)

class AnimalesListaViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(AnimalesListaUiState())
    val state: StateFlow<AnimalesListaUiState> = _state.asStateFlow()

    init { load() }

    fun onQuery(q: String) {
        _state.update { it.copy(query = q) }
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            container.syncStatus.syncNow()
            val list = if (_state.value.query.isBlank()) {
                container.animalRepository.getAll()
            } else {
                container.animalRepository.search(_state.value.query.trim())
            }
            _state.update {
                it.copy(
                    loading = false,
                    items = list.map { a ->
                        AnimalItemUi(
                            codigo = a.codigo,
                            title = animalDisplayTitle(a),
                            sexo = a.sexo,
                            detail = "Código ${a.codigo} · ${a.raza} · ${a.potrero}",
                            pendingSync = a.syncState == Animal.SyncState.PENDING.name,
                        )
                    },
                )
            }
        }
    }
}

data class AnimalDetalleUiState(
    val animal: Animal? = null,
    val tab: Int = 0,
    val loading: Boolean = true,
    val reproEvents: List<ReproductionEventLocal> = emptyList(),
    val productionRecords: List<ProductionLocal> = emptyList(),
)

class AnimalDetalleViewModel(
    savedStateHandle: SavedStateHandle,
    private val container: AppContainer,
) : ViewModel() {
    private val codigo: String = savedStateHandle.get<String>("codigo").orEmpty()
    private val _state = MutableStateFlow(AnimalDetalleUiState())
    val state: StateFlow<AnimalDetalleUiState> = _state.asStateFlow()

    init { load() }

    fun setTab(index: Int) = _state.update { it.copy(tab = index) }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            container.syncStatus.syncNow()
            val animal = container.animalDao.findByCodigo(codigo)
            val reproEvents = container.localCacheDao.allReproduction()
                .filter { it.animalCodigo == codigo }
                .sortedByDescending { it.programada }
            val productionRecords = container.localCacheDao.allProduction()
                .filter { rec ->
                    rec.animalCodigo.equals(codigo, ignoreCase = true) ||
                        (animal?.serverId?.isNotBlank() == true && rec.animalServerId == animal.serverId)
                }
                .sortedByDescending { it.fecha }
            _state.update {
                it.copy(animal = animal, reproEvents = reproEvents, productionRecords = productionRecords, loading = false)
            }
        }
    }
}

data class AnimalFormUiState(
    val codigo: String = "",
    val nombre: String = "",
    val tipo: String = "",
    val raza: String = "",
    val sexo: String = "Hembra",
    val peso: String = "",
    val fechaNacimiento: String = "",
    val potrero: String = "",
    val observacion: String = "",
    val potreroOptions: List<String> = emptyList(),
    val isEdit: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
    val loading: Boolean = false,
)

class AnimalFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val container: AppContainer,
) : ViewModel() {
    private val editCodigo: String? = savedStateHandle.get<String>("codigo")
    private val _state = MutableStateFlow(AnimalFormUiState(isEdit = editCodigo != null))
    val state: StateFlow<AnimalFormUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val potreros = container.localCacheDao.allPotreros().map { it.nombre }
            _state.update { it.copy(potreroOptions = potreros) }
            editCodigo?.let { code ->
                container.animalDao.findByCodigo(code)?.let { a ->
                    _state.update {
                        it.copy(
                            codigo = a.codigo,
                            nombre = a.nombre,
                            tipo = a.tipo,
                            raza = a.raza,
                            sexo = a.sexo,
                            peso = a.peso.toString(),
                            fechaNacimiento = a.fechaNacimiento,
                            potrero = a.potrero,
                            observacion = a.observacion,
                        )
                    }
                }
            }
        }
    }

    fun update(block: AnimalFormUiState.() -> AnimalFormUiState) = _state.update(block)

    fun save() {
        val s = _state.value
        val peso = s.peso.replace(",", ".").toDoubleOrNull()
        when {
            s.codigo.isBlank() -> _state.update { it.copy(error = "Ingresa el código") }
            s.tipo.isBlank() -> _state.update { it.copy(error = "Selecciona el tipo") }
            s.raza.isBlank() -> _state.update { it.copy(error = "Selecciona la raza") }
            s.potrero.isBlank() && s.potreroOptions.isNotEmpty() -> _state.update { it.copy(error = "Selecciona el potrero") }
            peso == null -> _state.update { it.copy(error = "Peso inválido") }
            s.fechaNacimiento.isBlank() -> _state.update { it.copy(error = "Ingresa la fecha de nacimiento") }
            else -> viewModelScope.launch {
                _state.update { it.copy(loading = true, error = null) }
                runCatching {
                    if (s.isEdit && editCodigo != null) {
                        container.animalRepository.updateAnimal(
                            editCodigo,
                            Animal(
                                codigo = s.codigo,
                                nombre = s.nombre,
                                tipo = s.tipo,
                                raza = s.raza,
                                sexo = s.sexo,
                                peso = peso,
                                fechaNacimiento = s.fechaNacimiento,
                                potrero = s.potrero,
                                observacion = s.observacion,
                            ),
                        )
                    } else {
                        container.animalRepository.insert(
                            Animal(
                                codigo = s.codigo,
                                nombre = s.nombre,
                                tipo = s.tipo,
                                raza = s.raza,
                                sexo = s.sexo,
                                peso = peso,
                                fechaNacimiento = s.fechaNacimiento,
                                potrero = s.potrero,
                                observacion = s.observacion,
                            ),
                        )
                    }
                }.onSuccess {
                    _state.update { it.copy(loading = false, saved = true) }
                }.onFailure { e ->
                    _state.update { it.copy(loading = false, error = e.message ?: "No se pudo guardar") }
                }
            }
        }
    }

    fun clearSaved() = _state.update { it.copy(saved = false) }
}
