package com.example.client_kurs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.client_kurs.domain.repository.AuthRepository
import com.example.client_kurs.domain.usecase.CreateOrderUseCase
import com.example.client_kurs.domain.usecase.GetProductsUseCase
import com.example.client_kurs.domain.usecase.UpdateStockUseCase
import com.example.client_kurs.domain.repository.ProductRepository
import com.example.client_kurs.domain.model.Product
import com.example.client_kurs.domain.model.OrderItem
import com.example.client_kurs.domain.model.UserRole
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun login(email: String, password: String, onResult: (UserRole?, String?) -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            authRepository.login(email, password).onSuccess { role ->
                onResult(role, null)
            }.onFailure { error ->
                onResult(null, error.message ?: "Ошибка входа")
            }
            _isLoading.value = false
        }
    }

    fun register(email: String, password: String, role: UserRole, onResult: (Boolean, String?) -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            authRepository.register(email, password, role).onSuccess {
                onResult(true, null)
            }.onFailure { error ->
                onResult(false, error.message ?: "Ошибка регистрации")
            }
            _isLoading.value = false
        }
    }

    fun getCurrentUserRole(): UserRole? {
        return authRepository.getCurrentUserRole()
    }

    fun logout() {
        authRepository.logout()
    }
}

class CustomerViewModel(
    private val getProductsUseCase: GetProductsUseCase,
    private val createOrderUseCase: CreateOrderUseCase
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    private val _cart = MutableStateFlow<Map<Product, Int>>(emptyMap())
    val cart = _cart.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        _isLoading.value = true
        viewModelScope.launch {
            getProductsUseCase().onSuccess {
                _products.value = it
            }
            _isLoading.value = false
        }
    }

    fun addToCart(product: Product) {
        val currentCart = _cart.value.toMutableMap()
        currentCart[product] = (currentCart[product] ?: 0) + 1
        _cart.value = currentCart
    }

    fun checkout(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val items = _cart.value.map { OrderItem(it.key.id, it.value) }
        if (items.isEmpty()) return

        _isLoading.value = true
        viewModelScope.launch {
            createOrderUseCase(items).onSuccess {
                _cart.value = emptyMap() // Очищаем корзину при успехе
                onSuccess()
            }.onFailure {
                onError(it.message ?: "Ошибка оформления заказа")
            }
            _isLoading.value = false
        }
    }
}

class StorekeeperViewModel(
    private val getProductsUseCase: GetProductsUseCase,
    private val updateStockUseCase: UpdateStockUseCase,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        _isLoading.value = true
        viewModelScope.launch {
            getProductsUseCase().onSuccess {
                _products.value = it
            }
            _isLoading.value = false
        }
    }

    fun updateStock(productId: Int, currentQuantity: Int, delta: Int) {
        val newQuantity = currentQuantity + delta
        if (newQuantity < 0) return

        _isLoading.value = true
        viewModelScope.launch {
            updateStockUseCase(productId, newQuantity).onSuccess {
                loadProducts()
            }
            _isLoading.value = false
        }
    }

    fun addProduct(name: String, price: Double, quantity: Int, onComplete: (String?) -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            productRepository.addProduct(name, price, quantity).onSuccess {
                loadProducts()
                onComplete(null)
            }.onFailure {
                onComplete(it.message ?: "Ошибка добавления товара")
            }
            _isLoading.value = false
        }
    }
}
