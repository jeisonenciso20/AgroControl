package com.jeison.agrocontrol.ui.feature.reproduccion

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jeison.agrocontrol.data.ReproductionEventLocal
import com.jeison.agrocontrol.ui.common.Formatters
import com.jeison.agrocontrol.ui.components.AgroCard
import com.jeison.agrocontrol.ui.components.EmptyState
import com.jeison.agrocontrol.ui.theme.AgroIcons
import com.jeison.agrocontrol.ui.theme.AgroSpacing

fun reproductionEventLabel(tipoEvento: String): String = when (tipoEvento.uppercase()) {
    "CELO" -> "Celo"
    "INSEMINACION" -> "Inseminación"
    "DIAGNOSTICO" -> "Diagnóstico gestación"
    "PARTO" -> "Parto"
    else -> tipoEvento.replace('_', ' ').lowercase().replaceFirstChar { it.titlecase() }
}

fun mapReproEstadoFromLast(last: ReproductionEventLocal?): String = when (last?.tipoEvento) {
    "CELO" -> "En celo"
    "INSEMINACION" -> "Inseminada"
    "DIAGNOSTICO" -> "Gestante"
    "PARTO" -> "Parida"
    else -> "Vacía"
}

@Composable
fun ReproduccionEventList(
    events: List<ReproductionEventLocal>,
    modifier: Modifier = Modifier,
) {
    if (events.isEmpty()) {
        EmptyState(
            AgroIcons.Reproduccion,
            "Sin eventos",
            "Registra el primer evento de reproducción",
            modifier = modifier.padding(AgroSpacing.md),
        )
        return
    }
    Column(modifier = modifier, verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(AgroSpacing.xs)) {
        events.forEach { ev ->
            AgroCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(AgroSpacing.sm)) {
                    Text(reproductionEventLabel(ev.tipoEvento), style = MaterialTheme.typography.titleSmall)
                    Text(
                        Formatters.formatDate(ev.programada),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (ev.notas.isNotBlank()) {
                        Text(
                            ev.notas,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = AgroSpacing.xxs),
                        )
                    }
                }
            }
        }
    }
}
