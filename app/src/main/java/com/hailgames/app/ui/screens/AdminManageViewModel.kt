package com.hailgames.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hailgames.app.data.AuthRepository
import com.hailgames.app.data.model.Profile
import com.hailgames.app.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminManageUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val profiles: List<Profile> = emptyList()
)

class AdminManageViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminManageUiState())
    val uiState: StateFlow<AdminManageUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            runCatching { authRepository.fetchAllProfiles() }
                .onSuccess { profiles ->
                    _uiState.value = AdminManageUiState(
                        isLoading = false,
                        profiles = profiles.sortedBy { it.username }
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Não foi possível carregar os usuários."
                    )
                }
        }
    }

    fun setRole(userId: String, role: UserRole) {
        viewModelScope.launch {
            runCatching { authRepository.setRole(userId, role) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        profiles = _uiState.value.profiles.map { profile ->
                            if (profile.id == userId) profile.copy(role = role.dbValue) else profile
                        }
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Não foi possível atualizar o papel."
                    )
                }
        }
    }
}
