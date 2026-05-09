package com.example.client_kurs.presentation.ui

import android.widget.Toast
<<<<<<< HEAD
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
=======
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
>>>>>>> 6d7f8b3 (Исправление логики отправки запросов на получение имеющихся товаров на складе)
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.client_kurs.presentation.viewmodel.CustomerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen(
    viewModel: CustomerViewModel,
<<<<<<< HEAD
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
=======
    onNavigateToCart: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onLogout: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current

    val totalItems = cart.values.sum()

    LaunchedEffect(error) {
        if (error != null) {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Каталог") },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "История"
                        )
                    }
                    BadgedBox(
                        badge = {
                            if (totalItems > 0) {
                                Badge { Text("$totalItems") }
                            }
                        }
                    ) {
                        IconButton(onClick = onNavigateToCart) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Корзина"
                            )
                        }
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Выход"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading && products.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(products) { product ->
                        ProductCard(
                            product = product,
                            onAddToCart = { viewModel.addToCart(product) }
                        )
>>>>>>> 6d7f8b3 (Исправление логики отправки запросов на получение имеющихся товаров на складе)
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
<<<<<<< HEAD
=======
    val error by viewModel.error.collectAsState()
    val checkoutSuccess by viewModel.checkoutSuccess.collectAsState()
    val context = LocalContext.current
>>>>>>> 6d7f8b3 (Исправление логики отправки запросов на получение имеющихся товаров на складе)

    val context = LocalContext.current
    val totalSum = cart.entries.sumOf { it.key.price * it.value }

<<<<<<< HEAD
=======
    LaunchedEffect(error) {
        if (error != null) {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(checkoutSuccess) {
        if (checkoutSuccess) {
            Toast.makeText(context, "Заказ успешно оформлен!", Toast.LENGTH_SHORT).show()
            viewModel.clearCheckoutSuccess()
            onNavigateBack()
        }
    }

>>>>>>> 6d7f8b3 (Исправление логики отправки запросов на получение имеющихся товаров на складе)
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
<<<<<<< HEAD
=======
            }
        }
    ) { padding ->
        if (cart.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Корзина пуста")
            }
        } else {
            LazyColumn(
                contentPadding = padding,
                modifier = Modifier.fillMaxSize()
            ) {
                items(cart.entries.toList(), key = { it.key.id }) { (product, quantity) ->
                    CartItem(
                        product = product,
                        quantity = quantity,
                        onIncrease = {
                            viewModel.updateCartQuantity(product, quantity + 1)
                        },
                        onDecrease = {
                            viewModel.updateCartQuantity(product, quantity - 1)
                        },
                        onRemove = { viewModel.removeFromCart(product) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.padding(bottom = 8.dp))
                }
>>>>>>> 6d7f8b3 (Исправление логики отправки запросов на получение имеющихся товаров на складе)
            }
        }
    }
}

