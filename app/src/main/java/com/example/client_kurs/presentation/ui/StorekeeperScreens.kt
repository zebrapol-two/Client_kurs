package com.example.client_kurs.presentation.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.client_kurs.presentation.viewmodel.StorekeeperViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorekeeperHomeScreen(
    viewModel: StorekeeperViewModel
) {
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Управление складом") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Text("+", modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    ) { padding ->
        if (isLoading && products.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(contentPadding = padding, modifier = Modifier.fillMaxSize()) {
                items(products) { product ->
                    val isLowStock = product.quantity < 5
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isLowStock) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = product.name, style = MaterialTheme.typography.titleMedium)
                                Text(text = "Цена: ${product.price} ₽", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = "Остаток: ${product.quantity} шт.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isLowStock) MaterialTheme.colorScheme.error else Color.Unspecified
                                )
                            }
                            Row {
                                IconButton(onClick = { viewModel.updateStock(product.id, product.quantity, -1) }, enabled = !isLoading && product.quantity > 0) {
                                    Text("-")
                                }
                                IconButton(onClick = { viewModel.updateStock(product.id, product.quantity, 1) }, enabled = !isLoading) {
                                    Text("+")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var price by remember { mutableStateOf("") }
        var quantity by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Добавить товар") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Название") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Цена") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Количество") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = !isLoading && name.isNotBlank() && price.isNotBlank() && quantity.isNotBlank(),
                    onClick = {
                        val pPrice = price.toDoubleOrNull() ?: 0.0
                        val pQuantity = quantity.toIntOrNull() ?: 0
                        viewModel.addProduct(name, pPrice, pQuantity) { error ->
                            if (error != null) {
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                            } else {
                                showAddDialog = false
                                Toast.makeText(context, "Товар добавлен!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

