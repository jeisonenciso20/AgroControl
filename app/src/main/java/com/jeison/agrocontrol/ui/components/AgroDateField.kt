package com.jeison.agrocontrol.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.jeison.agrocontrol.ui.common.Formatters
import com.jeison.agrocontrol.ui.theme.AgroIcons
import com.jeison.agrocontrol.ui.theme.AgroSpacing
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgroDateField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = AgroIcons.Fecha,
    error: String? = null,
) {
    var showPicker by remember { mutableStateOf(false) }
    val zone = remember { ZoneId.systemDefault() }
    val parsed = remember(value) {
        runCatching { LocalDate.parse(value.take(10)) }.getOrNull()
    }
    val display = when {
        parsed != null -> Formatters.formatDate(value)
        value.isBlank() -> ""
        else -> value
    }

    val openPicker: () -> Unit = { showPicker = true }

    AgroField(
        value = display,
        onValueChange = {},
        label = label,
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = openPicker,
        ),
        leadingIcon = leadingIcon,
        trailingIcon = {
            IconButton(onClick = openPicker) {
                Icon(AgroIcons.Fecha, contentDescription = "Elegir fecha")
            }
        },
        error = error,
        readOnly = true,
        enabled = true,
    )

    if (showPicker) {
        val initialMillis = (parsed ?: LocalDate.now())
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
                            onValueChange(date.toString())
                        }
                        showPicker = false
                    },
                ) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancelar") }
            },
        ) {
            Column {
                DatePicker(state = pickerState)
                TextButton(
                    onClick = {
                        onValueChange(LocalDate.now().toString())
                        showPicker = false
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = AgroSpacing.sm),
                ) {
                    Text("Usar fecha de hoy")
                }
            }
        }
    }
}
