package com.jeison.agrocontrol.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jeison.agrocontrol.ui.common.Formatters
import com.jeison.agrocontrol.ui.common.SyncBannerState
import com.jeison.agrocontrol.ui.theme.AgroIcons
import com.jeison.agrocontrol.ui.theme.AgroSizes
import com.jeison.agrocontrol.ui.theme.AgroSpacing
import com.jeison.agrocontrol.ui.theme.AgroTheme

enum class KpiAccent { PRIMARY, TERTIARY, SECONDARY, ERROR }

enum class KpiState { Value, Loading, Unavailable }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgroScaffold(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    fab: @Composable () -> Unit = {},
    isLoading: Boolean = false,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            title,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.semantics { heading() },
                        )
                    },
                    navigationIcon = {
                        if (onBack != null) {
                            IconButton(onClick = onBack, modifier = Modifier.size(AgroSizes.touchMin)) {
                                Icon(AgroIcons.Volver, contentDescription = "Volver")
                            }
                        }
                    },
                    actions = actions,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                )
                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        floatingActionButton = fab,
        containerColor = MaterialTheme.colorScheme.background,
        content = content,
    )
}

@Composable
fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AgroSpacing.md, vertical = AgroSpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction, modifier = Modifier.heightIn(min = AgroSizes.touchMin)) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
fun SyncStatusBanner(
    state: SyncBannerState,
    onRetry: () -> Unit,
    onSyncNow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    data class BannerUi(
        val bg: androidx.compose.ui.graphics.Color,
        val fg: androidx.compose.ui.graphics.Color,
        val icon: ImageVector,
        val text: String,
        val actionLabel: String? = null,
        val action: (() -> Unit)? = null,
    )
    val ui = when {
        !state.isOnline && state.pendingCount > 0 -> BannerUi(
            AgroTheme.colors.warningContainer,
            AgroTheme.colors.onWarningContainer,
            AgroIcons.SinInternet,
            "Sin internet. ${state.pendingCount} cambios guardados en el teléfono",
        )
        !state.isOnline -> BannerUi(
            AgroTheme.colors.offlineContainer,
            AgroTheme.colors.offline,
            AgroIcons.SinInternet,
            "Sin internet. Puedes seguir trabajando",
        )
        state.isSyncing -> BannerUi(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            AgroIcons.Sincronizar,
            "Enviando cambios...",
        )
        state.lastError != null -> BannerUi(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            AgroIcons.Error,
            "No se pudo sincronizar",
            "Reintentar",
            onRetry,
        )
        state.pendingCount > 0 -> BannerUi(
            AgroTheme.colors.warningContainer,
            AgroTheme.colors.onWarningContainer,
            AgroIcons.PorEnviar,
            "${state.pendingCount} cambios por enviar",
            "Enviar ahora",
            onSyncNow,
        )
        else -> return
    }
    Surface(modifier = modifier.fillMaxWidth(), color = ui.bg) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = AgroSizes.touchMin)
                .padding(horizontal = AgroSpacing.md, vertical = AgroSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(ui.icon, contentDescription = null, tint = ui.fg, modifier = Modifier.size(AgroSizes.iconSm))
            Spacer(Modifier.width(AgroSpacing.xs))
            Text(ui.text, style = MaterialTheme.typography.bodyMedium, color = ui.fg, modifier = Modifier.weight(1f))
            if (ui.actionLabel != null && ui.action != null) {
                TextButton(onClick = ui.action) { Text(ui.actionLabel) }
            }
        }
    }
}

@Composable
fun AgroCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    Card(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .heightIn(min = if (onClick != null) AgroSizes.listItemMin else 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = border,
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(AgroSpacing.md)) { content() }
    }
}

@Composable
fun KpiTile(
    icon: ImageVector,
    label: String,
    value: String,
    unit: String? = null,
    caption: String? = null,
    accent: KpiAccent = KpiAccent.PRIMARY,
    state: KpiState = KpiState.Value,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val (container, onContainer) = when (accent) {
        KpiAccent.PRIMARY -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        KpiAccent.TERTIARY -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        KpiAccent.SECONDARY -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        KpiAccent.ERROR -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }
    AgroCard(modifier = modifier.heightIn(min = 120.dp), onClick = onClick) {
        Column {
            Surface(shape = CircleShape, color = container, modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = onContainer)
                }
            }
            Spacer(Modifier.height(AgroSpacing.sm))
            when (state) {
                KpiState.Loading -> Text("...", style = MaterialTheme.typography.displaySmall)
                KpiState.Unavailable -> Text("-", style = MaterialTheme.typography.displaySmall)
                KpiState.Value -> Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        value,
                        style = MaterialTheme.typography.displaySmall.copy(fontFeatureSettings = "tnum"),
                    )
                    unit?.let {
                        Spacer(Modifier.width(4.dp))
                        Text(it, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Text(label, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            caption?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(AgroSizes.touchPrimary),
        shape = MaterialTheme.shapes.extraLarge,
    ) { Text(text, style = MaterialTheme.typography.labelLarge) }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(AgroSizes.touchPrimary),
        shape = MaterialTheme.shapes.extraLarge,
    ) { Text(text, style = MaterialTheme.typography.labelLarge) }
}

@Composable
fun AgroTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    error: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    readOnly: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        enabled = enabled,
        readOnly = readOnly,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = AgroSizes.touchPrimary),
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
        trailingIcon = trailingIcon,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        shape = MaterialTheme.shapes.small,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        singleLine = singleLine,
    )
}

@Composable
fun AgroField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    error: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    readOnly: Boolean = false,
) = AgroTextField(
    value,
    onValueChange,
    label,
    modifier,
    leadingIcon,
    trailingIcon,
    error,
    keyboardOptions,
    keyboardActions,
    visualTransformation,
    singleLine,
    enabled,
    readOnly,
)

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AgroSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(AgroSizes.iconXl), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(AgroSpacing.md))
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(AgroSpacing.lg))
            PrimaryButton(text = actionLabel, onClick = onAction, modifier = Modifier.fillMaxWidth(0.7f))
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

data class AnimalItemUi(
    val codigo: String,
    val title: String,
    val sexo: String,
    val detail: String,
    val pendingSync: Boolean,
)

@Composable
fun AnimalListItem(item: AnimalItemUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AgroCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(item.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(item.sexo, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(end = AgroSpacing.xs))
            Icon(AgroIcons.Abrir, contentDescription = "Abrir detalle")
        }
    }
}

data class TaskItemUi(
    val id: String,
    val titulo: String,
    val vencimiento: String,
    val statusLabel: String,
    val isOverdue: Boolean,
    val referencia: String = "",
    val observacion: String = "",
)

@Composable
fun TaskListItem(
    item: TaskItemUi,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    showCompleteCheck: Boolean = false,
    onComplete: (() -> Unit)? = null,
) {
    AgroCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showCompleteCheck && onComplete != null) {
                Checkbox(
                    checked = false,
                    onCheckedChange = { checked -> if (checked) onComplete() },
                    modifier = Modifier
                        .padding(end = AgroSpacing.xxs)
                        .semantics { contentDescription = "Marcar como terminado" },
                )
            }
            Icon(
                if (item.isOverdue) AgroIcons.Vencido else AgroIcons.VencePronto,
                contentDescription = null,
                tint = if (item.isOverdue) MaterialTheme.colorScheme.error else AgroTheme.colors.warning,
            )
            Spacer(Modifier.width(AgroSpacing.sm))
            Column(Modifier.weight(1f)) {
                Text(item.titulo, style = MaterialTheme.typography.titleSmall)
                Text("${item.statusLabel} · ${item.vencimiento}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (item.referencia.isNotBlank()) {
                    Text("Ref: ${item.referencia}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (item.observacion.isNotBlank()) {
                    Text(
                        item.observacion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                    )
                }
            }
        }
    }
}

@Composable
fun TaskDetailDialog(item: TaskItemUi, onDismiss: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text("Cerrar") }
        },
        title = { Text(item.titulo) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AgroSpacing.xs)) {
                Text("${item.statusLabel} · ${item.vencimiento}")
                if (item.referencia.isNotBlank()) {
                    Text("Ref: ${item.referencia}", style = MaterialTheme.typography.bodySmall)
                }
                if (item.observacion.isNotBlank()) {
                    Text("Observaciones", style = MaterialTheme.typography.labelMedium)
                    Text(item.observacion, style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text("Sin observaciones", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
    )
}

@Composable
fun AgroFabExtended(label: String, onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = Modifier.height(AgroSizes.fab),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        icon = { Icon(AgroIcons.Agregar, contentDescription = null) },
        text = { Text(label, style = MaterialTheme.typography.labelLarge) },
    )
}

@Composable
fun LastSyncText(lastPullIso: String?, modifier: Modifier = Modifier) {
    Text(
        Formatters.formatRelative(lastPullIso),
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

enum class StatusChipTone { SUCCESS, INFO, WARNING, NEUTRAL }

@Composable
fun StatusChip(text: String, tone: StatusChipTone, modifier: Modifier = Modifier) {
    val (bg, fg) = when (tone) {
        StatusChipTone.SUCCESS -> AgroTheme.colors.successContainer to AgroTheme.colors.success
        StatusChipTone.INFO -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        StatusChipTone.WARNING -> AgroTheme.colors.warningContainer to AgroTheme.colors.warning
        StatusChipTone.NEUTRAL -> MaterialTheme.colorScheme.surfaceContainerHigh to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraSmall,
        color = bg,
    ) {
        Text(
            text.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            modifier = Modifier.padding(horizontal = AgroSpacing.sm, vertical = AgroSpacing.xxs),
            style = MaterialTheme.typography.labelMedium,
            color = fg,
        )
    }
}

@Composable
fun ProgressBarRow(
    label: String,
    value: Double,
    max: Double,
    valueText: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth().padding(vertical = AgroSpacing.xs)) {
        Row(Modifier.fillMaxWidth()) {
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text(valueText, style = MaterialTheme.typography.bodyLarge)
        }
        LinearProgressIndicator(
            progress = { if (max > 0) (value / max).toFloat().coerceIn(0f, 1f) else 0f },
            modifier = Modifier.fillMaxWidth().padding(top = AgroSpacing.xxs),
            color = color,
        )
    }
}

@Composable
fun StatRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = AgroSizes.touchMin)
            .padding(vertical = AgroSpacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(AgroSizes.icon))
        Spacer(Modifier.width(AgroSpacing.sm))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}
