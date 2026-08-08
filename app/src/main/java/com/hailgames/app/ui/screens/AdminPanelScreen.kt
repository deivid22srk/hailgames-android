package com.hailgames.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hailgames.app.data.model.ContentItem
import com.hailgames.app.ui.components.ContentListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    isAdmin: Boolean,
    isOwner: Boolean,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (String) -> Unit,
    onManageAdmins: () -> Unit,
    viewModel: AdminPanelViewModel = viewModel(factory = adminPanelViewModelFactory())
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var itemToDelete by remember { mutableStateOf<ContentItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Painel ADM") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.load() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Atualizar")
                    }
                }
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(onClick = onAdd) {
                    Icon(Icons.Filled.Add, contentDescription = "Adicionar item")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isOwner) {
                Card(
                    onClick = onManageAdmins,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Text(
                        text = "Gerenciar admins",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            when {
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                state.error != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.error.orEmpty(),
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.size(12.dp))
                        OutlinedButton(onClick = { viewModel.load() }) {
                            Text("Tentar novamente")
                        }
                    }
                }

                else -> {
                    // Category filter chips
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = state.selectedCategoryId == null,
                                onClick = { viewModel.onCategorySelected(null) },
                                label = { Text("Tudo") }
                            )
                        }
                        items(state.categories) { category ->
                            FilterChip(
                                selected = state.selectedCategoryId == category.id,
                                onClick = { viewModel.onCategorySelected(category.id) },
                                label = { Text(category.name) }
                            )
                        }
                    }

                    if (state.filteredItems.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Nenhum item encontrado.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 96.dp)
                        ) {
                            itemsIndexed(state.filteredItems, key = { _, item -> item.id ?: "" }) { _, item ->
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ContentListItem(
                                        item = item,
                                        categoryName = state.categories.firstOrNull { it.id == item.categoryId }?.name,
                                        onClick = { item.id?.let(onEdit) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = { itemToDelete = item }) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Excluir",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    itemToDelete?.let { item ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Excluir item") },
            text = { Text("Excluir \"${item.title}\" permanentemente?") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    item.id?.let { viewModel.deleteItem(it) }
                    itemToDelete = null
                }) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
