package com.hailgames.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hailgames.app.data.ContentRepository
import com.hailgames.app.data.model.ContentItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ContentDetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val item: ContentItem? = null,
    val deleted: Boolean = false
)

class ContentDetailViewModel(
    private val itemId: String,
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContentDetailUiState())
    val uiState: StateFlow<ContentDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = ContentDetailUiState(isLoading = true)
            runCatching { contentRepository.fetchContentItem(itemId) }
                .onSuccess { item ->
                    _uiState.value = ContentDetailUiState(isLoading = false, item = item)
                }
                .onFailure { e ->
                    _uiState.value = ContentDetailUiState(
                        isLoading = false,
                        error = e.message ?: "Não foi possível carregar o item."
                    )
                }
        }
    }

    fun delete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            runCatching { contentRepository.deleteContentItem(itemId) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(deleted = true)
                    onDeleted()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Não foi possível excluir o item."
                    )
                }
        }
    }
}
