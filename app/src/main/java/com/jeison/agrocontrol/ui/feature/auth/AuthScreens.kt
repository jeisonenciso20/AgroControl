package com.jeison.agrocontrol.ui.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeison.agrocontrol.R
import com.jeison.agrocontrol.di.AppViewModelFactory
import com.jeison.agrocontrol.ui.components.AgroPhotoBackground
import com.jeison.agrocontrol.ui.components.AgroTextField
import com.jeison.agrocontrol.ui.components.AuthFormPanel
import com.jeison.agrocontrol.ui.components.PrimaryButton
import com.jeison.agrocontrol.ui.components.SecondaryButton
import com.jeison.agrocontrol.ui.theme.AgroIcons
import com.jeison.agrocontrol.ui.theme.AgroSpacing

@Composable
fun LoginRoute(onLoggedIn: () -> Unit, onRegister: () -> Unit, vm: AuthViewModel = viewModel(factory = AppViewModelFactory)) {
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.loggedIn) { if (state.loggedIn) { vm.clearLoggedInFlag(); onLoggedIn() } }
    LoginScreen(state, vm::onEmail, vm::onPassword, vm::login, onRegister)
}

@Composable
fun LoginScreen(
    state: AuthUiState,
    onEmail: (String) -> Unit,
    onPassword: (String) -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
) {
    var showPassword by remember { mutableStateOf(false) }
    AgroPhotoBackground(R.drawable.bg_fondo, scrimBottom = 0.55f) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(AgroSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AuthFormPanel {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(AgroIcons.Bovino, contentDescription = null, modifier = Modifier.size(72.dp))
                    Text("AgroControl", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Gestión de tu finca, aun sin internet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(AgroSpacing.xl))
                    AgroTextField(state.email, onEmail, "Correo electrónico", leadingIcon = AgroIcons.Correo, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next))
                    Spacer(Modifier.height(AgroSpacing.sm))
                    AgroTextField(
                        state.password,
                        onPassword,
                        "Contraseña",
                        leadingIcon = AgroIcons.Clave,
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(if (showPassword) AgroIcons.Ocultar else AgroIcons.Ver, contentDescription = "Mostrar contraseña")
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onLogin() }),
                    )
                    state.error?.let {
                        Spacer(Modifier.height(AgroSpacing.sm))
                        Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.small) {
                            Text(it, Modifier.padding(AgroSpacing.sm), color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                    Spacer(Modifier.height(AgroSpacing.lg))
                    PrimaryButton("Ingresar", onLogin, enabled = !state.loading)
                    Spacer(Modifier.height(AgroSpacing.md))
                    Text("¿No tienes cuenta?", style = MaterialTheme.typography.bodyMedium)
                    SecondaryButton("Crear cuenta", onRegister, enabled = !state.loading)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroRoute(onBack: () -> Unit, onLoggedIn: () -> Unit, vm: AuthViewModel = viewModel(factory = AppViewModelFactory)) {
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.loggedIn) { if (state.loggedIn) { vm.clearLoggedInFlag(); onLoggedIn() } }
    AgroPhotoBackground(R.drawable.bg_fondo2, scrimBottom = 0.5f) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Crear cuenta", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(AgroIcons.Volver, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = AgroSpacing.lg, vertical = AgroSpacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AuthFormPanel {
                    Column {
                        AgroTextField(state.nombre, vm::onNombre, "Tu nombre", leadingIcon = AgroIcons.Usuario)
                        Spacer(Modifier.height(AgroSpacing.sm))
                        AgroTextField(state.fincaNombre, vm::onFinca, "Nombre de la finca", leadingIcon = AgroIcons.Finca)
                        Spacer(Modifier.height(AgroSpacing.sm))
                        AgroTextField(state.email, vm::onEmail, "Correo electrónico", leadingIcon = AgroIcons.Correo, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                        Spacer(Modifier.height(AgroSpacing.sm))
                        AgroTextField(state.password, vm::onPassword, "Contraseña", leadingIcon = AgroIcons.Clave, visualTransformation = PasswordVisualTransformation())
                        Text("Mínimo 8 caracteres", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(AgroSpacing.sm))
                        AgroTextField(state.confirmPassword, vm::onConfirmPassword, "Confirmar contraseña", leadingIcon = AgroIcons.Clave, visualTransformation = PasswordVisualTransformation())
                        state.error?.let {
                            Spacer(Modifier.height(AgroSpacing.sm))
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(Modifier.height(AgroSpacing.lg))
                        PrimaryButton("Crear cuenta", vm::register, enabled = !state.loading)
                    }
                }
            }
        }
    }
}

@Composable
fun BienvenidaScreen(onContinue: () -> Unit, onLogin: (() -> Unit)? = null) {
    AgroPhotoBackground(R.drawable.bg_fondo, scrimBottom = 0.5f) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(AgroSpacing.lg),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Icon(AgroIcons.Bovino, contentDescription = null, modifier = Modifier.size(56.dp), tint = Color.White)
            Text("AgroControl", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text("Tu finca al día, aun sin señal", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.9f))
            Spacer(Modifier.height(AgroSpacing.lg))
            BenefitRow(AgroIcons.Bovino, "Registra tus animales")
            BenefitRow(AgroIcons.Leche, "Anota el ordeño diario")
            BenefitRow(AgroIcons.SinInternet, "Funciona sin internet")
            Spacer(Modifier.height(AgroSpacing.xl))
            PrimaryButton("Comenzar", onContinue)
            if (onLogin != null) {
                TextButton(onClick = onLogin, modifier = Modifier.fillMaxWidth()) {
                    Text("Ya tengo cuenta: Ingresar", color = Color.White)
                }
            }
            Spacer(Modifier.height(AgroSpacing.xxl))
        }
    }
}

@Composable
private fun BenefitRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AgroSpacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        Spacer(Modifier.size(AgroSpacing.sm))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = Color.White)
    }
}
