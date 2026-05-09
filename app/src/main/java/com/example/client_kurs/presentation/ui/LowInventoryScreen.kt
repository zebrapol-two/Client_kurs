package com.example.client_kurs.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.client_kurs.domain.model.Product
import com.example.client_kurs.presentation.viewmodel.StorekeeperViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LowInventoryScreen(
    viewModel: StorekeeperViewModel,
    onBack: () -> Unit
) {
    val lowInventoryProducts by viewModel.lowInventoryProducts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var thresholdInput by remember { mutableStateOf(StorekeeperViewModel.DEFAULT_LOW_STOCK_THRESHOLD.toString()) }
    // ключи – строковые идентификаторы продуктов
    val replenishInputs = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(Unit) {
        viewModel.getLowInventory(StorekeeperViewModel.DEFAULT_LOW_STOCK_THRESHOLD)
    }

    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(error ?: "")
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Низкий остаток") }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = thresholdInput,
                    onValueChange = { thresholdInput = it },
                    label = { Text("Порог") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        val threshold = thresholdInput.toIntOrNull() ?: StorekeeperViewModel.DEFAULT_LOW_STOCK_THRESHOLD
                        viewModel.getLowInventory(threshold)
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text("Применить")
                }
            }

            TextButton(onClick = onBack) { Text("Назад") }

            if (isLoading && lowInventoryProducts.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // Явно указываем тип продукта для ключа
                    items(lowInventoryProducts, key = { product: Product -> product.id }) { product ->
                        val replenishValue = replenishInputs[product.id] ?: "1"
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            ProductManageRow(
                                product = product,
                                isLoading = isLoading,
                                onIncrease = { viewModel.updateStock(product.id, 1) },
                                onDecrease = { viewModel.updateStock(product.id, -1) }
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = replenishValue,
                                    onValueChange = { replenishInputs[product.id] = it },
                                    label = { Text("Пополнить на N") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        val delta = replenishInputs[product.id]?.toIntOrNull()
                                        if (delta != null && delta > 0) {
                                            viewModel.updateStock(product.id, delta)
                                        }
                                    }
                                ) {
                                    Text("Пополнить")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}