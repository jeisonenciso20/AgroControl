package com.jeison.agrocontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.jeison.agrocontrol.ui.AgroApp
import com.jeison.agrocontrol.ui.common.ThemePreferences
import com.jeison.agrocontrol.ui.theme.AgroTheme
import com.jeison.agrocontrol.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as AgroControlApp
        setContent {
            val prefs = remember { ThemePreferences(app) }
            var themeMode by remember { mutableStateOf(prefs.getThemeMode()) }
            AgroTheme(themeMode = themeMode) {
                AgroApp(app, themeMode) { mode ->
                    themeMode = mode
                }
            }
        }
    }
}
