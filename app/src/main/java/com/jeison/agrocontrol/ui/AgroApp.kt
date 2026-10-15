package com.jeison.agrocontrol.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jeison.agrocontrol.AgroControlApp
import com.jeison.agrocontrol.ui.common.ThemePreferences
import com.jeison.agrocontrol.ui.components.SyncStatusBanner
import com.jeison.agrocontrol.ui.navigation.AgroNavHost
import com.jeison.agrocontrol.ui.navigation.Dest
import com.jeison.agrocontrol.ui.navigation.Graph
import com.jeison.agrocontrol.ui.theme.ThemeMode

@Composable
fun AgroApp(app: AgroControlApp, themeMode: ThemeMode, onThemeModeChange: (ThemeMode) -> Unit) {
    val container = app.container
    val prefs = remember { ThemePreferences(app) }
    val banner by container.syncStatus.bannerState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var startDest by remember {
        mutableStateOf(
            when {
                !prefs.isOnboardingSeen() -> Dest.Bienvenida.route
                app.tokenStore.isLoggedIn() -> Graph.MAIN
                else -> Dest.Login.route
            },
        )
    }

    LaunchedEffect(Unit) {
        container.connectivity.isOnline.collect { container.syncStatus.setOnline(it) }
    }

    LaunchedEffect(Unit) {
        container.syncStatus.refreshPending()
    }

    Column(Modifier.fillMaxSize()) {
        if (app.tokenStore.isLoggedIn()) {
            SyncStatusBanner(
                state = banner,
                onRetry = { scope.launch { container.syncStatus.syncNow() } },
                onSyncNow = { scope.launch { container.syncStatus.syncNow() } },
            )
        }
        AgroNavHost(
            modifier = Modifier.fillMaxSize(),
            startDestination = startDest,
            onThemeChanged = onThemeModeChange,
            onAuthChanged = {
                startDest = if (app.tokenStore.isLoggedIn()) Graph.MAIN else Dest.Login.route
            },
        )
    }
}
