package com.jeison.agrocontrol.ui.feature.mas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeison.agrocontrol.di.AppContainer
import com.jeison.agrocontrol.di.AppViewModelFactory
import com.jeison.agrocontrol.ui.common.Formatters
import com.jeison.agrocontrol.ui.common.ThemePreferences
import com.jeison.agrocontrol.ui.components.AgroScaffold
import com.jeison.agrocontrol.ui.components.ConfirmDialog
import com.jeison.agrocontrol.ui.components.PrimaryButton
import com.jeison.agrocontrol.ui.theme.AgroIcons
import com.jeison.agrocontrol.ui.theme.AgroSizes
import com.jeison.agrocontrol.ui.theme.AgroSpacing
import com.jeison.agrocontrol.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MasUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val lastSync: String? = null,
    val pending: Int = 0,
    val message: String? = null,
    val syncing: Boolean = false,
)

class MasViewModel(private val container: AppContainer) : ViewModel() {
    private val themePrefs = ThemePreferences(container.applicationContext)
    private val _state = MutableStateFlow(MasUiState())
    val state: StateFlow<MasUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            val pending = container.syncDao.pendingOutbox().size
            val last = container.syncDao.getMetadata("last_pull")?.value
            _state.update { it.copy(pending = pending, lastSync = last, themeMode = themePrefs.getThemeMode()) }
        }
    }

    fun cycleTheme(): ThemeMode {
        val next = when (_state.value.themeMode) {
            ThemeMode.SYSTEM -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.SYSTEM
        }
        themePrefs.setThemeMode(next)
        _state.update { it.copy(themeMode = next) }
        return next
    }

    fun syncNow() {
        viewModelScope.launch {
            _state.update { it.copy(syncing = true) }
            val result = container.syncStatus.syncNow()
            refresh()
            _state.update {
                it.copy(
                    syncing = false,
                    message = if (result.isSuccess) "Sincronización completada" else result.exceptionOrNull()?.message,
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            container.authSession.logout()
        }
    }
}

@Composable
fun MasRoute(
    onFinanzas: () -> Unit,
    onReportes: () -> Unit,
    onPendientes: () -> Unit,
    onReproduccion: () -> Unit,
    onLoggedOut: () -> Unit,
    onThemeChanged: (ThemeMode) -> Unit,
    vm: MasViewModel = viewModel(factory = AppViewModelFactory),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var showLogout by remember { mutableStateOf(false) }
    if (showLogout) {
        ConfirmDialog("Cerrar sesión", "¿Salir de tu cuenta?", "Salir", onConfirm = {
            vm.logout()
            showLogout = false
            onLoggedOut()
        }, onDismiss = { showLogout = false })
    }
    AgroScaffold(title = "Más", isLoading = state.syncing) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            Text(
                state.lastSync?.let { "Actualizado ${Formatters.formatRelative(it)}" } ?: "Nunca sincronizado",
                Modifier.padding(horizontal = AgroSpacing.md),
                style = MaterialTheme.typography.bodySmall,
            )
            PrimaryButton("Sincronizar ahora", vm::syncNow, modifier = Modifier.padding(AgroSpacing.md))
            state.message?.let { Text(it, Modifier.padding(horizontal = AgroSpacing.md), color = MaterialTheme.colorScheme.primary) }
            HorizontalDivider(Modifier.padding(vertical = AgroSpacing.sm))
            NavRow("Finanzas", AgroIcons.Finanzas, onFinanzas)
            NavRow("Reportes", AgroIcons.Reportes, onReportes)
            NavRow("Pendientes", AgroIcons.Pendientes, onPendientes)
            NavRow("Reproducción", AgroIcons.Reproduccion, onReproduccion)
            NavRow("Apariencia (${themeLabel(state.themeMode)})", AgroIcons.Tema) {
                onThemeChanged(vm.cycleTheme())
            }
            NavRow("Cerrar sesión", AgroIcons.Salir) { showLogout = true }
            Text("AgroControl v1.0", Modifier.padding(AgroSpacing.md), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun NavRow(title: String, icon: ImageVector, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = { Icon(AgroIcons.Abrir, contentDescription = null) },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = AgroSizes.touchMin)
            .clickable(onClick = onClick),
    )
}

private fun themeLabel(mode: ThemeMode) = when (mode) {
    ThemeMode.SYSTEM -> "Sistema"
    ThemeMode.LIGHT -> "Claro"
    ThemeMode.DARK -> "Oscuro"
}
