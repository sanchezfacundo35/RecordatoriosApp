
package com.facundo.recordatoriosapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facundo.recordatoriosapp.data.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val usuario: FirebaseUser? = null,
    val cargando: Boolean = false,
    val verificandoSesion: Boolean = true,
    val error: String? = null,
    val autenticado: Boolean = false
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.observarUsuario().collect { usuario ->
                _uiState.value = _uiState.value.copy(
                    usuario = usuario,
                    autenticado = usuario != null,
                    verificandoSesion = false,
                    cargando = false
                )
            }
        }
    }

    fun registrar(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                cargando = true,
                error = null
            )

            try {
                authRepository.registrar(email, password)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = e.localizedMessage
                        ?: "No se pudo completar el registro"
                )
            }
        }
    }

    fun iniciarSesion(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                cargando = true,
                error = null
            )

            try {
                authRepository.iniciarSesion(email, password)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = e.localizedMessage
                        ?: "No se pudo iniciar sesión"
                )
            }
        }
    }

    fun cerrarSesion() {
        authRepository.cerrarSesion()
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}