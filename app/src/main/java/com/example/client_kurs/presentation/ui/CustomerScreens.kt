package com.example.client_kurs.presentation.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.client_kurs.presentation.viewmodel.CustomerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen(
    viewModel: CustomerViewModel,
    onNavigateToCart: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val cart by viewModel.cart.collectAsState()

    val totalItems = cart.values.sum()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Каталог") })
        },
        floatingActionButton = {
            if (totalItems > 0) {
                FloatingActionButton(onClick = onNavigateToCart) {
                    Text(
                        text = "Корзина ($totalItems)",
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = padding, modifier = Modifier.fillMaxSize()) {
            items(products) { product ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = product.name, style = MaterialTheme.typography.titleMedium)
                            Text(text = "Цена: ${product.price} ₽", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "В наличии: ${product.quantity} шт.", style = MaterialTheme.typography.bodySmall)
                        }
                        Button(onClick = { viewModel.addToCart(product) }) {
                            Text("В корзину")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: CustomerViewModel,
    onNavigateBack: () -> Unit
) {
    val cart by viewModel.cart.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val context = LocalContext.current
    val totalSum = cart.entries.sumOf { it.key.price * it.value }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Корзина") })
        },
        bottomBar = {
            if (cart.isNotEmpty()) {
                BottomAppBar {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Итого: $totalSum ₽", style = MaterialTheme.typography.titleMedium)
                        Button(
                            onClick = {
                                viewModel.checkout(
                                    onSuccess = {
                                        Toast.makeText(context, "Заказ оформлен!", Toast.LENGTH_SHORT).show()
                                        onNavigateBack()
                                    },
                                    onError = {
                                        Toast.makeText(context, "Ошибка: $it", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            enabled = !isLoading
                        ) {
                            Text("Оформить")
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (cart.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Корзина пуста")
            }
        } else {
            LazyColumn(contentPadding = padding, modifier = Modifier.fillMaxSize()) {
                items(cart.entries.toList()) { (product, quantity) ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = product.name, style = MaterialTheme.typography.titleMedium)
                                Text(text = "Цена: ${product.price} ₽ x $quantity")
                            }
                            Text(text = "= ${product.price * quantity} ₽", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}

