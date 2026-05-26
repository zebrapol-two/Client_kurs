package com.example.client_kurs.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.client_kurs.domain.model.Product
import com.example.client_kurs.domain.model.SupplierOffer
import com.example.client_kurs.presentation.viewmodel.SupplierComparisonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierComparisonScreen(
    viewModel: SupplierComparisonViewModel,
    onBack: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val supplierOffers by viewModel.supplierOffers.collectAsState()
    val marketPrice by viewModel.marketPrice.collectAsState()
    val selectedSupplier by viewModel.selectedSupplier.collectAsState()
    val purchaseQuantityInput by viewModel.purchaseQuantityInput.collectAsState()
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
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Сравнение цен поставщиков") }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TextButton(onClick = onBack) {
                Text("Назад")
            }

            Text("Выберите товар", style = MaterialTheme.typography.titleMedium)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(products, key = { it.id }) { product ->
                    ProductSelectorRow(
                        product = product,
                        isSelected = selectedProduct?.id == product.id,
                        onClick = { viewModel.selectProduct(product.id) }
                    )
                }
            }

            if (selectedProduct != null) {
                Text(
                    text = "Рыночная цена: ${"%.2f".format(marketPrice)} ₽",
                    style = MaterialTheme.typography.titleSmall
                )
            }

            if (isLoading && supplierOffers.isEmpty()) {
                CircularProgressIndicator()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.6f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(supplierOffers, key = { it.supplierId }) { offer ->
                        SupplierOfferRow(
                            offer = offer,
                            marketPrice = marketPrice,
                            onOrderClick = { viewModel.openPurchaseDialog(offer) }
                        )
                    }
                }
            }
        }
    }

    if (selectedSupplier != null) {
        AlertDialog(
            onDismissRequest = viewModel::closePurchaseDialog,
            title = { Text("Заказать у поставщика") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(selectedSupplier!!.supplierName)
                    OutlinedTextField(
                        value = purchaseQuantityInput,
                        onValueChange = viewModel::onPurchaseQuantityChange,
                        label = { Text("Количество") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmPurchase) {
                    Text("Подтвердить")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::closePurchaseDialog) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
private fun ProductSelectorRow(
    product: Product,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val background = if (isSelected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("${product.name} (ID ${product.id})")
        Text("${product.price} ₽")
    }
}

@Composable
private fun SupplierOfferRow(
    offer: SupplierOffer,
    marketPrice: Double,
    onOrderClick: () -> Unit
) {
    val saving = marketPrice - offer.price
    val savingText = if (saving > 0) {
        "✅ Экономия ${"%.2f".format(saving)} руб"
    } else {
        "Без экономии"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Поставщик")
            Text(offer.supplierName)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Его цена")
            Text("${"%.2f".format(offer.price)} ₽")
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Рыночная цена")
            Text("${"%.2f".format(marketPrice)} ₽")
        }
        Text(savingText)
        Button(onClick = onOrderClick, modifier = Modifier.fillMaxWidth()) {
            Text("Заказать у этого поставщика")
        }
    }
}
