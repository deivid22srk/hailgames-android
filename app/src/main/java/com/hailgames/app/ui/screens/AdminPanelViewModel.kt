package com.hailgames.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hailgames.app.data.ContentRepository
import com.hailgames.app.data.model.Category
import com.hailgames.app.data.model.ContentItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminPanelUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val categories: List<Category> = emptyList(),
    val items: List<ContentItem> = emptyList(),
    val selectedCategoryId: String? = null
) {
    val filteredItems: List<ContentItem>
        get() = items.filter { item ->
            selectedCategoryId == null || item.categoryId == selectedCategoryId
        }
}

class AdminPanelViewModel(
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminPanelUiState())
    val uiState: StateFlow<AdminPanelUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            runCatching {
                val categories = contentRepository.fetchCategories()
                val items = contentRepository.fetchContentItems()
                categories to items
            }.onSuccess { (categories, items) ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    categories = categories,
                    items = items
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Não foi possível carregar o conteúdo."
                )
            }
        }
    }

    fun onCategorySelected(categoryId: String?) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            runCatching { contentRepository.deleteContentItem(itemId) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        items = _uiState.value.items.filterNot { it.id == itemId }
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Não foi possível excluir o item."
                    )
                }
        }
    }
}
