package com.jeison.agrocontrol.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jeison.agrocontrol.ui.feature.animales.AnimalDetalleRoute
import com.jeison.agrocontrol.ui.feature.animales.AnimalFormRoute
import com.jeison.agrocontrol.ui.feature.animales.AnimalesListaRoute
import com.jeison.agrocontrol.ui.common.ThemePreferences
import com.jeison.agrocontrol.ui.feature.auth.BienvenidaScreen
import androidx.compose.ui.platform.LocalContext
import com.jeison.agrocontrol.ui.feature.auth.LoginRoute
import com.jeison.agrocontrol.ui.feature.auth.RegistroRoute
import com.jeison.agrocontrol.ui.feature.finanzas.FinanzasRoute
import com.jeison.agrocontrol.ui.feature.inicio.InicioRoute
import com.jeison.agrocontrol.ui.feature.mas.MasRoute
import com.jeison.agrocontrol.ui.feature.perfil.PerfilRoute
import com.jeison.agrocontrol.ui.feature.pendientes.PendientesRoute
import com.jeison.agrocontrol.ui.feature.potreros.PotreroDetalleRoute
import com.jeison.agrocontrol.ui.feature.potreros.PotreroFormRoute
import com.jeison.agrocontrol.ui.feature.potreros.PotreroTrabajoRoute
import com.jeison.agrocontrol.ui.feature.potreros.PotrerosRoute
import com.jeison.agrocontrol.ui.feature.produccion.ProduccionRegistrarRoute
import com.jeison.agrocontrol.ui.feature.produccion.ProduccionRoute
import com.jeison.agrocontrol.ui.feature.reportes.ReportesRoute
import com.jeison.agrocontrol.ui.feature.reproduccion.ReproduccionDetalleRoute
import com.jeison.agrocontrol.ui.feature.reproduccion.ReproduccionRegistrarRoute
import com.jeison.agrocontrol.ui.feature.reproduccion.ReproduccionRoute
import com.jeison.agrocontrol.ui.theme.AgroIcons
import com.jeison.agrocontrol.ui.theme.ThemeMode

@Composable
fun AgroNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String,
    onThemeChanged: (ThemeMode) -> Unit,
    onAuthChanged: () -> Unit,
) {
    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable(Dest.Bienvenida.route) {
            val ctx = LocalContext.current
            BienvenidaScreen(
                onContinue = {
                    ThemePreferences(ctx).setOnboardingSeen()
                    navController.navigate(Dest.Registro.route) { popUpTo(Dest.Bienvenida.route) { inclusive = true } }
                },
                onLogin = {
                    ThemePreferences(ctx).setOnboardingSeen()
                    navController.navigate(Dest.Login.route) { popUpTo(Dest.Bienvenida.route) { inclusive = true } }
                },
            )
        }
        composable(Dest.Login.route) {
            LoginRoute(
                onLoggedIn = {
                    onAuthChanged()
                    navController.navigate(Graph.MAIN) { popUpTo(Dest.Login.route) { inclusive = true } }
                },
                onRegister = { navController.navigate(Dest.Registro.route) },
            )
        }
        composable(Dest.Registro.route) {
            RegistroRoute(
                onBack = { navController.popBackStack() },
                onLoggedIn = {
                    onAuthChanged()
                    navController.navigate(Graph.MAIN) { popUpTo(Dest.Login.route) { inclusive = true } }
                },
            )
        }
        composable(Graph.MAIN) {
            MainTabsNav(onThemeChanged = onThemeChanged, onLoggedOut = {
                onAuthChanged()
                navController.navigate(Dest.Login.route) { popUpTo(Graph.MAIN) { inclusive = true } }
            })
        }
    }
}

@Composable
private fun MainTabsNav(onThemeChanged: (ThemeMode) -> Unit, onLoggedOut: () -> Unit) {
    val tabNav = rememberNavController()
    val backStackEntry by tabNav.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val tabRoutes = MainTab.entries.map { it.route }
    val showBar = route in tabRoutes

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            NavHost(navController = tabNav, startDestination = Dest.Inicio.route) {
                composable(Dest.Inicio.route) {
                    InicioRoute(
                        onAnimales = { tabNav.navigate(Dest.Animales.route) },
                        onPendientes = { tabNav.navigate(Dest.Pendientes.route) },
                        onPotreros = { tabNav.navigate(Dest.Potreros.route) },
                        onProduccion = { tabNav.navigate(Dest.Produccion.route) },
                        onReproduccion = { tabNav.navigate(Dest.Reproduccion.route) },
                        onPerfil = { tabNav.navigate(Dest.Perfil.route) },
                    )
                }
                composable(Dest.Animales.route) {
                    AnimalesListaRoute(
                        onAdd = { tabNav.navigate(Dest.AnimalNuevo.route) },
                        onDetail = { tabNav.navigate(Dest.AnimalDetalle.create(it)) },
                    )
                }
                composable(Dest.AnimalNuevo.route) {
                    AnimalFormRoute(onBack = { tabNav.popBackStack() }, onSaved = { tabNav.popBackStack() })
                }
                composable(
                    Dest.AnimalDetalle.route,
                    arguments = listOf(navArgument("codigo") { type = NavType.StringType }),
                ) {
                    AnimalDetalleRoute(
                        codigo = it.arguments?.getString("codigo").orEmpty(),
                        onBack = { tabNav.popBackStack() },
                        onEdit = { c -> tabNav.navigate(Dest.AnimalEditar.create(c)) },
                        onRegistrarProduccion = { c -> tabNav.navigate(Dest.ProduccionRegistrar.create(c)) },
                    )
                }
                composable(
                    Dest.AnimalEditar.route,
                    arguments = listOf(navArgument("codigo") { type = NavType.StringType }),
                ) {
                    AnimalFormRoute(onBack = { tabNav.popBackStack() }, onSaved = { tabNav.popBackStack() })
                }
                composable(Dest.Produccion.route) {
                    ProduccionRoute(onRegistrar = { tabNav.navigate(Dest.ProduccionRegistrar.create()) })
                }
                composable(
                    Dest.ProduccionRegistrar.route,
                    arguments = listOf(navArgument("codigo") { type = NavType.StringType; defaultValue = "" }),
                ) {
                    ProduccionRegistrarRoute(onBack = { tabNav.popBackStack() }, onSaved = { tabNav.popBackStack() })
                }
                composable(Dest.Potreros.route) {
                    PotrerosRoute(
                        onDetail = { tabNav.navigate(Dest.PotreroDetalle.create(it)) },
                        onAdd = { tabNav.navigate(Dest.PotreroNuevo.route) },
                    )
                }
                composable(Dest.PotreroNuevo.route) {
                    PotreroFormRoute(onBack = { tabNav.popBackStack() }, onSaved = { tabNav.popBackStack() })
                }
                composable(
                    Dest.PotreroEditar.route,
                    arguments = listOf(navArgument("serverId") { type = NavType.StringType }),
                ) {
                    PotreroFormRoute(onBack = { tabNav.popBackStack() }, onSaved = { tabNav.popBackStack() })
                }
                composable(
                    Dest.PotreroTrabajo.route,
                    arguments = listOf(navArgument("potrero") { type = NavType.StringType; defaultValue = "" }),
                ) {
                    PotreroTrabajoRoute(onBack = { tabNav.popBackStack() }, onSaved = { tabNav.popBackStack() })
                }
                composable(
                    Dest.PotreroDetalle.route,
                    arguments = listOf(navArgument("serverId") { type = NavType.StringType }),
                ) {
                    PotreroDetalleRoute(
                        onBack = { tabNav.popBackStack() },
                        onEdit = { id -> tabNav.navigate(Dest.PotreroEditar.create(id)) },
                        onTrabajo = { nombre -> tabNav.navigate(Dest.PotreroTrabajo.create(nombre)) },
                    )
                }
                composable(Dest.Mas.route) {
                    MasRoute(
                        onFinanzas = { tabNav.navigate(Dest.Finanzas.route) },
                        onReportes = { tabNav.navigate(Dest.Reportes.route) },
                        onPendientes = { tabNav.navigate(Dest.Pendientes.route) },
                        onReproduccion = { tabNav.navigate(Dest.Reproduccion.route) },
                        onLoggedOut = onLoggedOut,
                        onThemeChanged = onThemeChanged,
                    )
                }
                composable(Dest.Pendientes.route) { PendientesRoute(onBack = { tabNav.popBackStack() }) }
                composable(Dest.Perfil.route) { PerfilRoute(onBack = { tabNav.popBackStack() }) }
                composable(Dest.Finanzas.route) { FinanzasRoute(onBack = { tabNav.popBackStack() }) }
                composable(Dest.Reportes.route) { ReportesRoute(onBack = { tabNav.popBackStack() }) }
                composable(Dest.Reproduccion.route) {
                    ReproduccionRoute(
                        onBack = { tabNav.popBackStack() },
                        onRegistrar = { tabNav.navigate(Dest.ReproduccionRegistrar.create()) },
                        onDetalle = { c -> tabNav.navigate(Dest.ReproduccionDetalle.create(c)) },
                    )
                }
                composable(
                    Dest.ReproduccionDetalle.route,
                    arguments = listOf(navArgument("codigo") { type = NavType.StringType }),
                ) {
                    ReproduccionDetalleRoute(
                        onBack = { tabNav.popBackStack() },
                        onRegistrar = { c -> tabNav.navigate(Dest.ReproduccionRegistrar.create(c)) },
                    )
                }
                composable(
                    Dest.ReproduccionRegistrar.route,
                    arguments = listOf(navArgument("codigo") { type = NavType.StringType; defaultValue = "" }),
                ) {
                    ReproduccionRegistrarRoute(onBack = { tabNav.popBackStack() }, onSaved = { tabNav.popBackStack() })
                }
            }
        }
        if (showBar) {
            MainBottomBar(tabNav, route)
        }
    }
}

@Composable
private fun MainBottomBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar {
        MainTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo(Dest.Inicio.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        when (tab) {
                            MainTab.Inicio -> AgroIcons.Inicio
                            MainTab.Animales -> AgroIcons.Bovino
                            MainTab.Produccion -> AgroIcons.Produccion
                            MainTab.Potreros -> AgroIcons.Potrero
                            MainTab.Mas -> AgroIcons.Mas
                        },
                        contentDescription = tab.label,
                    )
                },
                label = { Text(tab.label) },
                alwaysShowLabel = true,
            )
        }
    }
}
