package com.jeison.agrocontrol.ui.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeison.agrocontrol.di.AppContainer
import com.jeison.agrocontrol.network.LoginRequest
import com.jeison.agrocontrol.network.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val nombre: String = "",
    val fincaNombre: String = "",
    val confirmPassword: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val loggedIn: Boolean = false,
)

class AuthViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState(loggedIn = container.tokenStore.isLoggedIn()))
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun onEmail(v: String) = _state.update { it.copy(email = v, error = null) }
    fun onPassword(v: String) = _state.update { it.copy(password = v, error = null) }
    fun onNombre(v: String) = _state.update { it.copy(nombre = v, error = null) }
    fun onFinca(v: String) = _state.update { it.copy(fincaNombre = v, error = null) }
    fun onConfirmPassword(v: String) = _state.update { it.copy(confirmPassword = v, error = null) }

    fun login() {
        val s = _state.value
        if (s.email.isBlank() || s.password.isBlank()) {
            _state.update { it.copy(error = "Completa correo y contraseña") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching {
                container.api.login(LoginRequest(s.email.trim(), s.password))
            }.onSuccess { tokens ->
                container.authSession.completeAuth(tokens, isRegister = false)
                _state.update { it.copy(loading = false, loggedIn = true) }
            }.onFailure { e ->
                _state.update { it.copy(loading = false, error = e.message ?: "Error de login") }
            }
        }
    }

    fun register() {
        val s = _state.value
        when {
            s.nombre.length < 2 -> _state.update { it.copy(error = "Ingresa tu nombre") }
            s.fincaNombre.length < 2 -> _state.update { it.copy(error = "Ingresa el nombre de la finca") }
            !s.email.contains("@") -> _state.update { it.copy(error = "Correo inválido") }
            s.password.length < 8 -> _state.update { it.copy(error = "Mínimo 8 caracteres") }
            s.password != s.confirmPassword -> _state.update { it.copy(error = "Las contraseñas no coinciden") }
            else -> viewModelScope.launch {
                _state.update { it.copy(loading = true, error = null) }
                runCatching {
                    container.api.register(
                        RegisterRequest(s.email.trim(), s.password, s.nombre.trim(), s.fincaNombre.trim()),
                    )
                }.onSuccess { tokens ->
                    container.authSession.completeAuth(tokens, isRegister = true)
                    _state.update { it.copy(loading = false, loggedIn = true) }
                }.onFailure { e ->
                    _state.update { it.copy(loading = false, error = e.message ?: "Registro falló") }
                }
            }
        }
    }

    fun clearLoggedInFlag() = _state.update { it.copy(loggedIn = false) }
}
