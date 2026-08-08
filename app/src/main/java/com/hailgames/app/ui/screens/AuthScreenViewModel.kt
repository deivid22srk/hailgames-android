package com.hailgames.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hailgames.app.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AuthMode { LOGIN, SIGNUP }

data class AuthUiState(
    val mode: AuthMode = AuthMode.LOGIN,
    val email: String = "",
    val password: String = "",
    val username: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthScreenViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onModeChange(mode: AuthMode) {
        _uiState.value = _uiState.value.copy(mode = mode, error = null)
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, error = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null)
    }

    fun onUsernameChange(value: String) {
        _uiState.value = _uiState.value.copy(username = value, error = null)
    }

    fun submit(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.isLoading) return

        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "Preencha e-mail e senha.")
            return
        }
        if (state.password.length < 6) {
            _uiState.value = state.copy(error = "A senha deve ter pelo menos 6 caracteres.")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            runCatching {
                when (state.mode) {
                    AuthMode.LOGIN -> authRepository.signIn(state.email.trim(), state.password)
                    AuthMode.SIGNUP -> authRepository.signUp(
                        state.email.trim(),
                        state.password,
                        state.username.ifBlank { null }
                    )
                }
            }.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erro inesperado. Tente novamente."
                )
            }
        }
    }
}
