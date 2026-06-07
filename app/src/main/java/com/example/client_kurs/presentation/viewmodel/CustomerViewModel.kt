package com.example.client_kurs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.client_kurs.domain.model.Order
import com.example.client_kurs.domain.model.Product
import com.example.client_kurs.domain.usecase.CreateOrderUseCase
import com.example.client_kurs.domain.usecase.GetMyOrdersUseCase
import com.example.client_kurs.domain.usecase.GetProductsUseCase
import com.example.client_kurs.data.repository.OrderRepositoryImpl
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CustomerViewModel(
    private val getProductsUseCase: GetProductsUseCase,
    private val createOrderUseCase: CreateOrderUseCase,
    private val getMyOrdersUseCase: GetMyOrdersUseCase,
    private val orderRepositoryImpl: OrderRepositoryImpl   // добавили для observeOrders
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    // Cart stored in memory only
    private val _cart = MutableStateFlow<Map<Product, Int>>(emptyMap())
    val cart = _cart.asStateFlow()

    // Подписываемся на поток заказов из Room
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders = _orders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isOrdersLoading = MutableStateFlow(false)
    val isOrdersLoading = _isOrdersLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _checkoutSuccess = MutableStateFlow(false)
    val checkoutSuccess = _checkoutSuccess.asStateFlow()

    init {
        loadProducts()
        // Подписываемся на локальный поток заказов
        viewModelScope.launch {
            orderRepositoryImpl.observeOrders().collect { ordersList ->
                _orders.value = ordersList
            }
        }
        // При старте – попытка обновить с сервера (работает фоном)
        refreshOrdersFromServer()
    }

    fun loadProducts() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            getProductsUseCase()
                .onSuccess { _products.value = it }
                .onFailure { _error.value = it.message ?: "Ошибка загрузки товаров" }
            _isLoading.value = false
        }
    }

    fun refreshOrdersFromServer() {
        viewModelScope.launch {
            _isOrdersLoading.value = true
            _error.value = null
            getMyOrdersUseCase()
                .onFailure { _error.value = it.message ?: "Ошибка загрузки заказов" }
            _isOrdersLoading.value = false
        }
    }

    fun addToCart(product: Product) {
        val current = _cart.value.toMutableMap()
        val currentQty = current[product] ?: 0
        val available = product.quantity
        if (currentQty < available) {
            current[product] = currentQty + 1
            _cart.value = current
        }
    }

    fun updateCartQuantity(product: Product, quantity: Int) {
        val current = _cart.value.toMutableMap()
        when {
            quantity <= 0 -> current.remove(product)
            quantity > product.quantity -> current[product] = product.quantity
            else -> current[product] = quantity
        }
        _cart.value = current
    }

    fun removeFromCart(product: Product) {
        val current = _cart.value.toMutableMap()
        current.remove(product)
        _cart.value = current
    }

    fun clearError() {
        _error.value = null
    }

    fun clearCheckoutSuccess() {
        _checkoutSuccess.value = false
    }

    fun checkout() {
        val cartSnapshot = _cart.value
        if (cartSnapshot.isEmpty()) return

        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            createOrderUseCase(cartSnapshot)
                .onSuccess {
                    _cart.value = emptyMap()
                    _checkoutSuccess.value = true
                    loadProducts()
                    refreshOrdersFromServer()  // после заказа обновляем историю
                }
                .onFailure {
                    _error.value = it.message ?: "Ошибка оформления заказа"
                }
            _isLoading.value = false
        }
    }
}