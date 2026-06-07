package com.example.client_kurs.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.client_kurs.domain.model.DisplayProduct
import com.example.client_kurs.domain.model.SupplierOffer
import com.example.client_kurs.presentation.viewmodel.SupplierComparisonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierComparisonScreen(
    viewModel: SupplierComparisonViewModel,
    onBack: () -> Unit
) {
    val allProducts by viewModel.allProducts.collectAsState()
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val supplierOffers by viewModel.supplierOffers.collectAsState()
    val marketPrice by viewModel.marketPrice.collectAsState()
    val selectedSupplier by viewModel.selectedSupplier.collectAsState()
    val purchaseQuantityInput by viewModel.purchaseQuantityInput.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSearching by viewModel.isLoadingSearch.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchError by viewModel.searchError.collectAsState()

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
        topBar = {
            TopAppBar(
                title = { Text("Сравнение цен") },
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                label = { Text("Поиск товара (FakeStoreAPI)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            if (isSearching) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (searchError != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = searchError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { viewModel.retrySearch() }) {
                        Text("Повторить")
                    }
                }
            }

            Text("Товары", style = MaterialTheme.typography.titleMedium)

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.45f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(allProducts, key = { "${it.isLocal}_${it.id}" }) { product ->
                    ProductDisplayRow(
                        product = product,
                        isSelected = selectedProduct?.id == product.id,
                        onClick = { viewModel.selectProduct(product) }
                    )
                }
            }

            val currentSelectedProduct = selectedProduct
            if (currentSelectedProduct != null) {
                Text(
                    text = "Рыночная цена: ${"%.2f".format(marketPrice)} ₽",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.fillMaxWidth()
                )

                if (!isLoading && supplierOffers.isEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { viewModel.purchaseAtMarketPrice() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Закупить по рыночной цене")
                    }
                }
            }

            if (isLoading && supplierOffers.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.55f),
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
private fun ProductDisplayRow(
    product: DisplayProduct,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer
    else MaterialTheme.colorScheme.surfaceVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = buildString {
                append(product.name)
                if (product.isLocal) append(" (склад)")
            },
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.width(8.dp))
        when {
            product.price != null -> Text("${"%.2f".format(product.price)} ₽", maxLines = 1)
            product.marketPrice != null -> Text("${"%.2f".format(product.marketPrice)} ₽", maxLines = 1)
            else -> Text(product.id, maxLines = 1, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun SupplierOfferRow(
    offer: SupplierOffer,
    marketPrice: Double,
    onOrderClick: () -> Unit
) {
    val saving = marketPrice - offer.price
    val roundedSaving = (saving * 100).toInt() / 100.0
    val isSaving = roundedSaving > 0.01
    val savingText = if (isSaving) "Экономия ${"%.2f".format(roundedSaving)} руб" else "Без экономии"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("Поставщик: ${offer.supplierName}", style = MaterialTheme.typography.bodyLarge)
        Text("Цена: ${"%.2f".format(offer.price)} ₽", style = MaterialTheme.typography.bodyMedium)
        Text(savingText)
        Button(onClick = onOrderClick, modifier = Modifier.fillMaxWidth()) {
            Text("Заказать у этого поставщика")
        }
    }
}