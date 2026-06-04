package com.example.client_kurs.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.client_kurs.domain.model.UserRole
import com.example.client_kurs.presentation.viewmodel.StorekeeperViewModel

@Composable
fun StorekeeperHomeScreen(
    viewModel: StorekeeperViewModel,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var searchQuery by remember { mutableStateOf("") }

    // Первая загрузка товаров происходит в ViewModel.init, здесь НЕ вызываем loadProducts()
    // Будущий я, если я еще раз хочу вставить черезмерное обновление склада
    // Засунь себе носок в рот, ей богу

    val filteredProducts = remember(products, searchQuery) {
        val query = searchQuery.trim().lowercase()
        if (query.isBlank()) {
            products
        } else {
            products.filter {
                it.id.toString().contains(query) ||
                        it.name.lowercase().contains(query)
            }
        }
    }

    val suggestions = remember(products, searchQuery) {
        val query = searchQuery.trim().lowercase()
        if (query.isBlank()) {
            emptyList()
        } else {
            products.asSequence()
                .filter {
                    it.id.toString().contains(query) ||
                            it.name.lowercase().contains(query)
                }
                .map { "${it.id} · ${it.name}" }
                .distinct()
                .take(5)
                .toList()
        }
    }

    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(error ?: "")
            viewModel.clearError()
        }
    }

    MainScaffold(
        role = UserRole.STOREKEEPER,
        title = "Склад",
        currentRoute = currentRoute,
        snackbarHostState = snackbarHostState,
        onNavigate = onNavigate,
        onLogout = onLogout,
        topBarSearch = {
            StockKeeperSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Поиск по ID или названию",
                suggestions = suggestions,
                onSuggestionClick = { selected ->
                    searchQuery = selected.substringAfter("·", selected).trim()
                }
            )
        },
        topBarActions = {
            IconButton(onClick = { snackbarHostState.currentSnackbarData?.dismiss() }) {
                Icon(Icons.Default.Notifications, contentDescription = "Уведомления")
            }
            IconButton(onClick = { onNavigate(NavigationRoutes.SETTINGS) }) {
                Icon(Icons.Default.Settings, contentDescription = "Настройки")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { onNavigate(NavigationRoutes.STOREKEEPER_ADD) },
                    label = { Text("Добавить") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
                AssistChip(
                    onClick = { onNavigate(NavigationRoutes.STOREKEEPER_LOW_INVENTORY) },
                    label = { Text("Низкий остаток") }
                )
                AssistChip(
                    onClick = { onNavigate(NavigationRoutes.STOREKEEPER_INVENTORY) },
                    label = { Text("Инвентаризация") }
                )
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredProducts, key = { it.id }) { product ->
                    AnimatedVisibility(visible = true) {
                        ProductManageRow(
                            product = product,
                            isLoading = isLoading,
                            onIncrease = { viewModel.updateStock(product.id, 1) },
                            onDecrease = { viewModel.updateStock(product.id, -1) }
                        )
                    }
                }
            }
        }
    }
}