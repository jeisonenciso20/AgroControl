package com.jeison.agrocontrol.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

enum class ThemeMode { SYSTEM, LIGHT, DARK }

val LocalAgroColors = staticCompositionLocalOf { LightExtended }

object AgroTheme {
    val colors: AgroExtendedColors
        @Composable
        get() = LocalAgroColors.current
}

@Composable
fun AgroTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colorScheme = if (dark) DarkColors else LightColors
    val extended = if (dark) DarkExtended else LightExtended
    CompositionLocalProvider(LocalAgroColors provides extended) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AgroTypography,
            shapes = AgroShapes,
            content = content,
        )
    }
}
