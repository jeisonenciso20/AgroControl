package com.jeison.agrocontrol.ui.feature.inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeison.agrocontrol.di.AppViewModelFactory
import com.jeison.agrocontrol.ui.components.KpiAccent
import com.jeison.agrocontrol.ui.components.KpiTile
import com.jeison.agrocontrol.ui.components.LastSyncText
import com.jeison.agrocontrol.ui.components.SectionHeader
import com.jeison.agrocontrol.ui.components.TaskListItem
import com.jeison.agrocontrol.ui.theme.AgroIcons
import com.jeison.agrocontrol.ui.theme.AgroSpacing

@Composable
fun InicioRoute(
    onAnimales: () -> Unit,
    onPendientes: () -> Unit,
    onPotreros: () -> Unit,
    onProduccion: () -> Unit,
    onReproduccion: () -> Unit,
    onPerfil: () -> Unit,
    vm: InicioViewModel = viewModel(factory = AppViewModelFactory),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(Unit) {
        vm.refresh()
        onPauseOrDispose { }
    }
    InicioScreen(
        state = state,
        onRefresh = vm::refresh,
        onAnimales = onAnimales,
        onPendientes = onPendientes,
        onPotreros = onPotreros,
        onProduccion = onProduccion,
        onReproduccion = onReproduccion,
        onPerfil = onPerfil,
        onCompleteTask = vm::completeTask,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioScreen(
    state: InicioUiState,
    onRefresh: () -> Unit,
    onAnimales: () -> Unit,
    onPendientes: () -> Unit,
    onPotreros: () -> Unit,
    onProduccion: () -> Unit,
    onReproduccion: () -> Unit,
    onPerfil: () -> Unit,
    onCompleteTask: (String) -> Unit = {},
) {
    PullToRefreshBox(isRefreshing = state.refreshing, onRefresh = onRefresh, modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = AgroSpacing.md),
        ) {
            item {
                InicioHomeHeader(
                    saludo = state.saludo,
                    fechaHoy = state.fechaHoy,
                    onPendientes = onPendientes,
                    onPerfil = onPerfil,
                )
                LastSyncText(state.lastSync, Modifier.padding(horizontal = AgroSpacing.md))
            }
            item {
                SectionHeader("Pendientes de hoy", "Ver todos", onPendientes)
                if (state.pendientes.isEmpty()) {
                    Text(
                        "No hay trabajos de potrero para hoy",
                        modifier = Modifier.padding(horizontal = AgroSpacing.md),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    state.pendientes.forEach { task ->
                        TaskListItem(
                            task,
                            onClick = onPendientes,
                            modifier = Modifier.padding(horizontal = AgroSpacing.md, vertical = AgroSpacing.xxs),
                            showCompleteCheck = true,
                            onComplete = { onCompleteTask(task.id) },
                        )
                    }
                }
            }
            item {
                SectionHeader("Resumen")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AgroSpacing.md, vertical = AgroSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(AgroSpacing.sm),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(AgroSpacing.sm)) {
                        KpiTile(
                            AgroIcons.Bovino,
                            "Animales",
                            state.animalesCount,
                            caption = "registrados",
                            accent = KpiAccent.PRIMARY,
                            state = state.animalesState,
                            onClick = onAnimales,
                            modifier = Modifier.weight(1f),
                        )
                        KpiTile(
                            AgroIcons.Leche,
                            "Leche semana",
                            state.lecheSemana,
                            unit = "L",
                            accent = KpiAccent.TERTIARY,
                            state = state.lecheState,
                            onClick = onProduccion,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(AgroSpacing.sm)) {
                        KpiTile(
                            AgroIcons.Potrero,
                            "Potreros",
                            state.potrerosCount,
                            caption = "registrados",
                            accent = KpiAccent.SECONDARY,
                            state = state.potrerosState,
                            onClick = onPotreros,
                            modifier = Modifier.weight(1f),
                        )
                        KpiTile(
                            AgroIcons.Reproduccion,
                            "Reproducción",
                            state.reproCount,
                            caption = "eventos",
                            accent = KpiAccent.ERROR,
                            state = state.reproState,
                            onClick = onReproduccion,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InicioHomeHeader(
    saludo: String,
    fechaHoy: String,
    onPendientes: () -> Unit,
    onPerfil: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = AgroSpacing.md)
            .padding(top = AgroSpacing.sm, bottom = AgroSpacing.md),
    ) {
        Column(Modifier.weight(1f)) {
            Text(saludo, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
            Text(fechaHoy, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onPendientes) {
            Icon(AgroIcons.Notificaciones, contentDescription = "Pendientes")
        }
        IconButton(onClick = onPerfil) {
            Icon(AgroIcons.Usuario, contentDescription = "Perfil")
        }
    }
}
