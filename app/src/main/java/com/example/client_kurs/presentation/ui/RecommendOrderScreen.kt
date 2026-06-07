package com.example.client_kurs.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.client_kurs.domain.model.RecommendedOrder
import com.example.client_kurs.presentation.viewmodel.RecommendOrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendOrderScreen(
    viewModel: RecommendOrderViewModel,
    onBack: () -> Unit
) {
    val items by viewModel.recommendedOrders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(error ?: "")
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Рекомендованный заказ") }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TextButton(onClick = onBack) { Text("Назад") }

            if (isLoading && items.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            } else if (items.isEmpty()) {
                Text("Нет товаров, требующих заказа поставщику")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items, key = { it.productId }) { item ->
                        RecommendOrderRow(
                            item = item,
                            isLoading = isLoading,
                            onCreateOrder = { viewModel.createSupplierOrder(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendOrderRow(
    item: RecommendedOrder,
    isLoading: Boolean,
    onCreateOrder: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(item.productName, style = MaterialTheme.typography.titleMedium)
        Text("Текущий остаток: ${item.currentStock}")
        Text("Минимальный запас: ${item.minimumStock}")
        Text("Средние продажи/день: ${"%.2f".format(item.averageSalesPerDay)}")
        Text("Срок доставки: ${item.deliveryDays} дн.")
        Text("Рекомендованный заказ: ${item.recommendedOrder} шт.")
        Button(
            onClick = onCreateOrder,
            enabled = !isLoading && item.recommendedOrder > 0,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Создать заказ поставщику")
        }
    }
}