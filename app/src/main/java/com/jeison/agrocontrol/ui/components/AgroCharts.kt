package com.jeison.agrocontrol.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jeison.agrocontrol.ui.common.Formatters
import com.jeison.agrocontrol.ui.theme.AgroSpacing
import kotlin.math.min

data class ChartSegment(val label: String, val value: Double, val color: Color)

@Composable
fun AgroBarChart(
    segments: List<ChartSegment>,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
    contentDescription: String,
) {
    if (segments.isEmpty()) return
    val max = segments.maxOf { it.value }.coerceAtLeast(1.0)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { this.contentDescription = contentDescription },
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(height),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom,
        ) {
            segments.forEach { seg ->
                val fraction = (seg.value / max).toFloat().coerceIn(0.05f, 1f)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = AgroSpacing.xxs),
                ) {
                    Text(
                        Formatters.formatNumber(seg.value),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Box(
                        Modifier
                            .weight(1f)
                            .width(28.dp)
                            .padding(top = AgroSpacing.xxs),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Canvas(Modifier.fillMaxWidth().fillMaxHeight()) {
                            val barHeight = size.height * fraction
                            drawRoundRect(
                                color = seg.color,
                                topLeft = Offset(0f, size.height - barHeight),
                                size = Size(size.width, barHeight),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                            )
                        }
                    }
                    Text(
                        seg.label.replaceFirstChar { it.titlecase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
fun AgroDonutChart(
    segments: List<ChartSegment>,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    centerTitle: String = "Total",
    contentDescription: String,
) {
    if (segments.isEmpty()) return
    val total = segments.sumOf { it.value }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { this.contentDescription = contentDescription },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
            Canvas(Modifier.size(size)) {
                val stroke = min(this.size.width, this.size.height) * 0.12f
                val diameter = min(this.size.width, this.size.height) - stroke
                val topLeft = Offset((this.size.width - diameter) / 2f, (this.size.height - diameter) / 2f)
                var startAngle = -90f
                segments.forEach { seg ->
                    val sweep = if (total > 0) (seg.value / total * 360f).toFloat() else 0f
                    if (sweep > 0f) {
                        drawArc(
                            color = seg.color,
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(diameter, diameter),
                            style = Stroke(width = stroke, cap = StrokeCap.Butt),
                        )
                        startAngle += sweep
                    }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(centerTitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(Formatters.formatNumber(total), style = MaterialTheme.typography.titleMedium)
            }
        }
        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = AgroSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AgroSpacing.xxs),
        ) {
            segments.forEach { seg ->
                val pct = if (total > 0) seg.value / total * 100 else 0.0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(Modifier.size(12.dp)) {
                        drawCircle(seg.color)
                    }
                    Text(
                        "${seg.label.replaceFirstChar { it.titlecase() }} · ${Formatters.formatNumber(seg.value)} (${Formatters.formatNumber(pct)}%)",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = AgroSpacing.sm),
                    )
                }
            }
        }
    }
}
