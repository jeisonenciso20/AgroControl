package com.jeison.agrocontrol.ui.feature.animales

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeison.agrocontrol.di.AppViewModelFactory
import com.jeison.agrocontrol.ui.common.Formatters
import com.jeison.agrocontrol.ui.components.AgroFabExtended
import com.jeison.agrocontrol.ui.components.AgroScaffold
import com.jeison.agrocontrol.ui.components.AgroDateField
import com.jeison.agrocontrol.ui.components.AgroField
import com.jeison.agrocontrol.ui.components.AnimalListItem
import com.jeison.agrocontrol.ui.components.EmptyState
import com.jeison.agrocontrol.ui.components.PrimaryButton
import com.jeison.agrocontrol.ui.feature.produccion.AnimalProductionList
import com.jeison.agrocontrol.ui.feature.reproduccion.ReproduccionEventList
import com.jeison.agrocontrol.ui.components.StatRow
import com.jeison.agrocontrol.ui.theme.AgroIcons
import com.jeison.agrocontrol.ui.theme.AgroSpacing

@Composable
fun AnimalesListaRoute(onAdd: () -> Unit, onDetail: (String) -> Unit, vm: AnimalesListaViewModel = viewModel(factory = AppViewModelFactory)) {
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.load() }
    AgroScaffold(
        title = "Animales",
        fab = { AgroFabExtended("Agregar animal", onAdd) },
        isLoading = state.loading,
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            AgroField(state.query, vm::onQuery, "Buscar animal", leadingIcon = AgroIcons.Buscar, modifier = Modifier.padding(AgroSpacing.md))
            if (state.items.isEmpty()) {
                EmptyState(AgroIcons.Bovino, "Sin animales", "Agrega el primero o sincroniza con el servidor", "Agregar animal", onAdd)
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = AgroSpacing.md, vertical = AgroSpacing.xs), verticalArrangement = Arrangement.spacedBy(AgroSpacing.xs)) {
                    items(state.items, key = { it.codigo }) { item ->
                        AnimalListItem(item, onClick = { onDetail(item.codigo) })
                    }
                }
            }
        }
    }
}

@Composable
fun AnimalDetalleRoute(
    codigo: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onRegistrarProduccion: (String) -> Unit = {},
    vm: AnimalDetalleViewModel = viewModel(factory = AppViewModelFactory),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val animal = state.animal
    LifecycleResumeEffect(codigo) {
        vm.load()
        onPauseOrDispose { }
    }
    AgroScaffold(title = animal?.let { animalDisplayTitle(it) } ?: codigo, onBack = onBack, actions = {
        if (animal != null) {
            androidx.compose.material3.IconButton(onClick = { onEdit(codigo) }) {
                androidx.compose.material3.Icon(AgroIcons.Editar, contentDescription = "Editar")
            }
        }
    }) { padding ->
        if (animal == null) {
            EmptyState(AgroIcons.Bovino, "Animal no encontrado", "Vuelve a la lista")
        } else {
            Column(Modifier.padding(padding).fillMaxSize()) {
                TabRow(selectedTabIndex = state.tab) {
                    Tab(state.tab == 0, { vm.setTab(0) }, text = { Text("Información") })
                    Tab(state.tab == 1, { vm.setTab(1) }, text = { Text("Producción") })
                    Tab(state.tab == 2, { vm.setTab(2) }, text = { Text("Reproducción") })
                }
                when (state.tab) {
                    0 -> Column(Modifier.verticalScroll(rememberScrollState()).padding(AgroSpacing.md)) {
                        StatRow(AgroIcons.Codigo, "Código", animal.codigo)
                        StatRow(AgroIcons.Raza, "Raza", animal.raza)
                        StatRow(AgroIcons.Peso, "Peso", "${Formatters.formatNumber(animal.peso)} kg")
                        StatRow(AgroIcons.Fecha, "Nacimiento", Formatters.formatDate(animal.fechaNacimiento))
                        StatRow(AgroIcons.Potrero, "Potrero", animal.potrero)
                        if (animal.observacion.isNotBlank()) StatRow(AgroIcons.Nota, "Observación", animal.observacion)
                    }
                    1 -> Column(Modifier.verticalScroll(rememberScrollState()).padding(AgroSpacing.md)) {
                        PrimaryButton(
                            "Registrar producción",
                            { onRegistrarProduccion(animal.codigo) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(AgroSpacing.md))
                        AnimalProductionList(state.productionRecords)
                    }
                    else -> Column(Modifier.verticalScroll(rememberScrollState()).padding(AgroSpacing.md)) {
                        ReproduccionEventList(state.reproEvents)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalFormRoute(onBack: () -> Unit, onSaved: () -> Unit, vm: AnimalFormViewModel = viewModel(factory = AppViewModelFactory)) {
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) { if (state.saved) { vm.clearSaved(); onSaved() } }
    val tipos = listOf("Vaca", "Toro", "Ternero", "Ternera")
    val sexos = listOf("Hembra", "Macho")
    AgroScaffold(title = if (state.isEdit) "Editar animal" else "Agregar animal", onBack = onBack) { padding ->
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(AgroSpacing.md)) {
            AgroField(state.codigo, { vm.update { copy(codigo = it) } }, "Código", leadingIcon = AgroIcons.Codigo, enabled = !state.isEdit)
            Spacer(Modifier.height(AgroSpacing.sm))
            AgroField(state.nombre, { vm.update { copy(nombre = it) } }, "Nombre (opcional)")
            Spacer(Modifier.height(AgroSpacing.sm))
            DropdownField("Tipo", state.tipo, tipos) { selected ->
                val sexoSugerido = when (selected) {
                    "Vaca", "Ternera" -> "Hembra"
                    "Toro", "Ternero" -> "Macho"
                    else -> state.sexo
                }
                vm.update { copy(tipo = selected, sexo = sexoSugerido) }
            }
            Spacer(Modifier.height(AgroSpacing.sm))
            DropdownField("Sexo", state.sexo, sexos) { vm.update { copy(sexo = it) } }
            Spacer(Modifier.height(AgroSpacing.sm))
            val razas = listOf("Holstein", "Jersey", "Brahman", "Normando", "Pardo Suizo", "Criollo", "Otra")
            DropdownField("Raza", state.raza.ifBlank { razas.first() }, razas) { vm.update { copy(raza = it) } }
            Spacer(Modifier.height(AgroSpacing.sm))
            AgroField(state.peso, { vm.update { copy(peso = it) } }, "Peso (kg)", leadingIcon = AgroIcons.Peso)
            Spacer(Modifier.height(AgroSpacing.sm))
            AgroDateField(state.fechaNacimiento, { vm.update { copy(fechaNacimiento = it) } }, "Fecha de nacimiento")
            Spacer(Modifier.height(AgroSpacing.sm))
            DropdownField("Potrero", state.potrero, state.potreroOptions) { vm.update { copy(potrero = it) } }
            Spacer(Modifier.height(AgroSpacing.sm))
            AgroField(state.observacion, { vm.update { copy(observacion = it) } }, "Observación", leadingIcon = AgroIcons.Nota, singleLine = false)
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(AgroSpacing.lg))
            PrimaryButton("Guardar animal", vm::save, enabled = !state.loading)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DropdownField(label: String, value: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        AgroField(
            value = value,
            onValueChange = {},
            label = label,
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            readOnly = true,
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { opt ->
                DropdownMenuItem(text = { Text(opt) }, onClick = { onSelect(opt); expanded = false })
            }
        }
    }
}
