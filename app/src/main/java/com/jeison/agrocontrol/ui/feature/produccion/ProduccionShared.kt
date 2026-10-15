package com.jeison.agrocontrol.ui.feature.produccion

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jeison.agrocontrol.data.ProductionLocal
import com.jeison.agrocontrol.ui.common.Formatters
import com.jeison.agrocontrol.ui.components.AgroCard
import com.jeison.agrocontrol.ui.components.EmptyState
import com.jeison.agrocontrol.ui.theme.AgroIcons
import com.jeison.agrocontrol.ui.theme.AgroSpacing

private fun jornadaLabel(jornada: String): String = when (jornada.uppercase()) {
    "MANANA" -> "Mañana"
    "TARDE" -> "Tarde"
    else -> jornada
}

@Composable
fun AnimalProductionList(
    records: List<ProductionLocal>,
    modifier: Modifier = Modifier,
) {
    if (records.isEmpty()) {
        EmptyState(
            AgroIcons.Leche,
            "Sin registros",
            "Aún no hay producción registrada para este animal",
            modifier = modifier.padding(AgroSpacing.md),
        )
        return
    }
    Column(
        modifier = modifier,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(AgroSpacing.xs),
    ) {
        records.forEach { record ->
            AgroCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(AgroIcons.Leche, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(AgroSpacing.sm))
                    Column(Modifier.weight(1f)) {
                        Text(
                            record.productType.replaceFirstChar { it.titlecase() },
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            "${Formatters.formatDate(record.fecha)} · ${jornadaLabel(record.jornada)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (record.notas.isNotBlank()) {
                            Text(
                                record.notas,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                            )
                        }
                    }
                    Text(
                        "${Formatters.formatNumber(record.cantidad)} ${record.unidad}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}
