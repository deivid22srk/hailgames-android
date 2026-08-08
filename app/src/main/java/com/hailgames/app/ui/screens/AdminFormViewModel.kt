package com.hailgames.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hailgames.app.data.ContentRepository
import com.hailgames.app.data.StorageRepository
import com.hailgames.app.data.model.Category
import com.hailgames.app.data.model.ContentItem
import com.hailgames.app.data.model.ContentItemInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminFormUiState(
    val isLoading: Boolean = true,
    val saving: Boolean = false,
    val error: String? = null,
    val categories: List<Category> = emptyList(),
    val isEditing: Boolean = false,
    val title: String = "",
    val description: String = "",
    val categoryId: String? = null,
    val coverUrl: String = "",
    val linkUrl: String = "",
    val fileUrl: String = "",
    val author: String = "",
    val version: String = "",
    val sizeMb: String = "",
    val saved: Boolean = false
)

class AdminFormViewModel(
    private val itemId: String?,
    private val contentRepository: ContentRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminFormUiState(isEditing = itemId != null))
    val uiState: StateFlow<AdminFormUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            runCatching { contentRepository.fetchCategories() }
                .onSuccess { categories ->
                    var state = _uiState.value.copy(isLoading = false, categories = categories)
                    if (itemId != null) {
                        runCatching { contentRepository.fetchContentItem(itemId) }
                            .onSuccess { item ->
                                state = _uiState.value.copy(
                                    isLoading = false,
                                    categories = categories,
                                    isEditing = true,
                                    title = item.title,
                                    description = item.description.orEmpty(),
                                    categoryId = item.categoryId,
                                    coverUrl = item.coverUrl.orEmpty(),
                                    linkUrl = item.linkUrl.orEmpty(),
                                    fileUrl = item.fileUrl.orEmpty(),
                                    author = item.author.orEmpty(),
                                    version = item.version.orEmpty(),
                                    sizeMb = item.sizeMb?.let {
                                        if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString()
                                    }.orEmpty()
                                )
                            }
                            .onFailure { e ->
                                state = _uiState.value.copy(
                                    isLoading = false,
                                    error = e.message ?: "Não foi possível carregar o item."
                                )
                            }
                    }
                    _uiState.value = state
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Não foi possível carregar as categorias."
                    )
                }
        }
    }

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value) }
    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }
    fun onCategoryChange(value: String?) = _uiState.update { it.copy(categoryId = value) }
    fun onCoverUrlChange(value: String) = _uiState.update { it.copy(coverUrl = value) }
    fun onLinkUrlChange(value: String) = _uiState.update { it.copy(linkUrl = value) }
    fun onFileUrlChange(value: String) = _uiState.update { it.copy(fileUrl = value) }
    fun onAuthorChange(value: String) = _uiState.update { it.copy(author = value) }
    fun onVersionChange(value: String) = _uiState.update { it.copy(version = value) }
    fun onSizeMbChange(value: String) = _uiState.update { it.copy(sizeMb = value) }

    fun save() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(error = "O título é obrigatório.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(saving = true, error = null) }
            val input = ContentItemInput(
                title = state.title.trim(),
                description = state.description.trim().ifBlank { null },
                coverUrl = state.coverUrl.trim().ifBlank { null },
                categoryId = state.categoryId,
                linkUrl = state.linkUrl.trim().ifBlank { null },
                fileUrl = state.fileUrl.trim().ifBlank { null },
                author = state.author.trim().ifBlank { null },
                version = state.version.trim().ifBlank { null },
                sizeMb = state.sizeMb.trim().toDoubleOrNull()
            )

            val result = runCatching {
                if (itemId != null) {
                    contentRepository.updateContentItem(itemId, input)
                } else {
                    contentRepository.createContentItem(input)
                }
            }

            result
                .onSuccess {
                    _uiState.update { it.copy(saving = false, saved = true) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(saving = false, error = e.message ?: "Não foi possível salvar o item.")
                    }
                }
        }
    }

    private fun MutableStateFlow<AdminFormUiState>.update(transform: (AdminFormUiState) -> AdminFormUiState) {
        value = transform(value)
    }
}
