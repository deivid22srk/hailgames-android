package com.hailgames.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hailgames.app.data.AuthRepository
import com.hailgames.app.data.model.Profile
import com.hailgames.app.data.model.UserRole
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SessionUiState(
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = true,
    val profile: Profile? = null,
    val role: UserRole = UserRole.USER
) {
    val isAdmin: Boolean get() = role.isAdmin
    val isOwner: Boolean get() = role == UserRole.OWNER
}

class SessionViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState(isLoading = true))
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        refreshProfile()
                    }
                    is SessionStatus.NotAuthenticated,
                    is SessionStatus.RefreshFailure,
                    is SessionStatus.Unknown -> {
                        _uiState.value = SessionUiState(isAuthenticated = false, isLoading = false)
                    }
                }
            }
        }
    }

    fun refreshProfile() {
        viewModelScope.launch {
            val profile = authRepository.fetchCurrentProfile()
            _uiState.value = SessionUiState(
                isAuthenticated = profile != null,
                isLoading = false,
                profile = profile,
                role = profile?.userRole ?: UserRole.USER
            )
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.value = SessionUiState(isAuthenticated = false, isLoading = false)
        }
    }
}
