package com.jeison.agrocontrol.ui.common

import android.content.Context
import com.jeison.agrocontrol.ui.theme.ThemeMode

class ThemePreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isOnboardingSeen(): Boolean = prefs.getBoolean(KEY_ONBOARDING, false)

    fun setOnboardingSeen() {
        prefs.edit().putBoolean(KEY_ONBOARDING, true).apply()
    }

    fun getThemeMode(): ThemeMode = when (prefs.getString(KEY_THEME, "system")) {
        "light" -> ThemeMode.LIGHT
        "dark" -> ThemeMode.DARK
        else -> ThemeMode.SYSTEM
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME, when (mode) {
            ThemeMode.LIGHT -> "light"
            ThemeMode.DARK -> "dark"
            ThemeMode.SYSTEM -> "system"
        }).apply()
    }

    companion object {
        private const val PREFS = "agrocontrol_ui_prefs"
        private const val KEY_ONBOARDING = "onboarding_visto"
        private const val KEY_THEME = "theme_mode"
    }
}
