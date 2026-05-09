package com.example.client_kurs.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.client_kurs.domain.model.InventoryAdjustment
import com.example.client_kurs.domain.model.Product
import com.example.client_kurs.domain.model.UserRole
import com.example.client_kurs.presentation.viewmodel.InventoryViewModel

@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val actualInputs by viewModel.actualInputs.collectAsState()
    val searchIdInput by viewModel.searchIdInput.collectAsState()
    val highlightedProductId by viewModel.highlightedProductId.collectAsState()
    val discrepancies by viewModel.discrepancies.collectAsState()
    val showReportDialog by viewModel.showReportDialog.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(error ?: "")
            viewModel.clearError()
        }
    }

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            snackbarHostState.showSnackbar(successMessage ?: "")
            viewModel.clearSuccessMessage()
            viewModel.loadProducts()
        }
    }

    MainScaffold(
        role = UserRole.STOREKEEPER,
        title = "Инвентаризация",
        currentRoute = currentRoute,
        snackbarHostState = snackbarHostState,
        onNavigate = onNavigate,
        onLogout = onLogout
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchIdInput,
                    onValueChange = viewModel::onSearchIdInputChange,
                    label = { Text("ID товара") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = viewModel::findById) {
                    Text("Найти")
                }
            }

            if (isLoading && products.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        AnimatedVisibility(visible = true) {
                            InventoryRow(
                                product = product,
                                actualInput = actualInputs[product.id].orEmpty(),
                                isHighlighted = highlightedProductId == product.id,
                                onActualInputChange = { viewModel.onActualInputChange(product.id, it) }
                            )
                        }
                    }
                }
            }

            Button(
                onClick = viewModel::prepareFinish,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Завершить инвентаризацию")
            }
        }
    }

    if (showReportDialog) {
        InventoryReportDialog(
            discrepancies = discrepancies,
            onDismiss = viewModel::dismissReportDialog,
            onConfirm = viewModel::confirmFinish
        )
    }
}

@Composable
private fun InventoryRow(
    product: Product,
    actualInput: String,
    isHighlighted: Boolean,
    onActualInputChange: (String) -> Unit
) {
    val containerColor = if (isHighlighted) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = containerColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = "ID: ${product.id}", style = MaterialTheme.typography.labelMedium)
        Text(text = product.name, style = MaterialTheme.typography.titleMedium)
        Text(text = "Ожидается: ${product.quantity} шт")
        Text(text = "Цена: ${product.price} ₽")
        OutlinedTextField(
            value = actualInput,
            onValueChange = onActualInputChange,
            label = { Text("Фактическое количество") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun InventoryReportDialog(
    discrepancies: List<InventoryAdjustment>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val totalAmount = discrepancies.sumOf { it.amountDifference }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Акт расхождений") },
        text = {
            if (discrepancies.isEmpty()) {
                Text("Расхождений не найдено. Отправить пустой акт?")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    discrepancies.forEach {
                        Text(
                            text = "${it.productName}: ${it.expectedQuantity} → ${it.actualQuantity}, " +
                                "Δ ${it.quantityDifference} шт, ${"%.2f".format(it.amountDifference)} ₽"
                        )
                    }
                    Text(
                        text = "Итог по сумме: ${"%.2f".format(totalAmount)} ₽",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Подтвердить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
