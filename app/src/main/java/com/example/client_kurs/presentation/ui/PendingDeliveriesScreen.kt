package com.example.client_kurs.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.client_kurs.data.remote.dto.PurchaseOrderDto
import com.example.client_kurs.presentation.viewmodel.PendingDeliveriesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingDeliveriesScreen(
    viewModel: PendingDeliveriesViewModel,
    onBack: () -> Unit,
    onDeliveryReceived: () -> Unit
) {
    val deliveries by viewModel.deliveries.collectAsState()
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
        topBar = {
            TopAppBar(
                title = { Text("Ожидаемые поставки") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isLoading && deliveries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (deliveries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Нет ожидающих поставок", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(deliveries, key = { it.id }) { delivery ->
                        DeliveryCard(
                            delivery = delivery,
                            isLoading = isLoading,
                            onReceiveAll = {
                                viewModel.receiveAll(delivery.id)
                                onDeliveryReceived()
                            },
                            onReceivePartial = { qty ->
                                viewModel.receivePartial(delivery.id, qty)
                                onDeliveryReceived()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeliveryCard(
    delivery: PurchaseOrderDto,
    isLoading: Boolean,
    onReceiveAll: () -> Unit,
    onReceivePartial: (Int) -> Unit
) {
    val remaining = delivery.orderedQuantity - delivery.receivedQuantity
    val canReceive = remaining > 0 && !isLoading

    var partialQty by remember { mutableStateOf("") }
    var showPartialDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = delivery.productName,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Поставщик: ${delivery.supplierName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Заказано: ${delivery.orderedQuantity} шт.")
                Text("Принято: ${delivery.receivedQuantity} шт.")
            }
            if (remaining > 0) {
                Text(
                    "Осталось принять: $remaining шт.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Кнопка "Принять всё"
            Button(
                onClick = onReceiveAll,
                enabled = canReceive,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Принять всё ($remaining шт.)")
            }

            // Кнопка частичной приёмки
            TextButton(
                onClick = { showPartialDialog = true },
                enabled = canReceive
            ) {
                Text("Принять частично")
            }
        }
    }

    // Диалог для частичной приёмки
    if (showPartialDialog) {
        AlertDialog(
            onDismissRequest = { showPartialDialog = false },
            title = { Text("Частичная приёмка") },
            text = {
                Column {
                    Text("Введите количество (макс. $remaining):")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = partialQty,
                        onValueChange = { if (it.all { c -> c.isDigit() }) partialQty = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val qty = partialQty.toIntOrNull()
                        if (qty != null && qty in 1..remaining) {
                            onReceivePartial(qty)
                            showPartialDialog = false
                            partialQty = ""
                        }
                    },
                    enabled = partialQty.isNotBlank()
                ) {
                    Text("Принять")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPartialDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}