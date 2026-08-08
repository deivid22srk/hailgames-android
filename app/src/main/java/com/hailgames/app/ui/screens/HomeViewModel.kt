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

data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val categories: List<Category> = emptyList(),
    val items: List<ContentItem> = emptyList(),
    val selectedCategoryId: String? = null,
    val searchQuery: String = ""
) {
    val filteredItems: List<ContentItem>
        get() = items.filter { item ->
            val matchesCategory = selectedCategoryId == null || item.categoryId == selectedCategoryId
            val matchesSearch = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                (item.description?.contains(searchQuery, ignoreCase = true) == true)
            matchesCategory && matchesSearch
        }
}

class HomeViewModel(
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

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

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
}
