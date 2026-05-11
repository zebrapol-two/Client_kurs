package com.example.client_kurs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.client_kurs.domain.model.Product
import com.example.client_kurs.domain.usecase.AddProductUseCase
import com.example.client_kurs.domain.usecase.GetLowInventoryUseCase
import com.example.client_kurs.domain.usecase.GetProductsUseCase
import com.example.client_kurs.domain.usecase.UpdateProductStockUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StorekeeperViewModel(
    private val getProductsUseCase: GetProductsUseCase,
    private val updateProductStockUseCase: UpdateProductStockUseCase,
    private val addProductUseCase: AddProductUseCase,
    private val getLowInventoryUseCase: GetLowInventoryUseCase
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    private val _lowInventoryProducts = MutableStateFlow<List<Product>>(emptyList())
    val lowInventoryProducts = _lowInventoryProducts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var selectedThreshold: Int = DEFAULT_LOW_STOCK_THRESHOLD

    init {
        // Загружаем товары один раз при создании ViewModel
        loadProducts()
    }

    fun clearError() {
        _error.value = null
    }

    fun loadProducts() {
        println("loadProducts called from Thread: ${Thread.currentThread().name}")
        _isLoading.value = true
        viewModelScope.launch {
            val productsResult = getProductsUseCase()
            productsResult.onSuccess {
                _products.value = it
            }.onFailure {
                _error.value = it.message ?: "Не удалось загрузить товары"
            }
            refreshLowInventoryInternal()
            _isLoading.value = false
        }
    }

    fun updateStock(productId: String, delta: Int) {
        val currentProduct = _products.value.find { it.id == productId }
        if (delta < 0 && currentProduct != null && currentProduct.quantity + delta < 0) {
            _error.value = "Остаток не может быть отрицательным"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            updateProductStockUseCase(productId, delta).onSuccess {
                loadProducts()
            }.onFailure {
                _error.value = it.message ?: "Не удалось обновить остаток"
                _isLoading.value = false
            }
        }
    }

    fun addProduct(
        productId: String,
        name: String,
        price: Double,
        quantity: Int,
        onSuccess: () -> Unit = {}
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            addProductUseCase(productId, name, price, quantity).onSuccess {
                // Сначала обновляем список, потом закрываем экран
                loadProducts()
                onSuccess()
            }.onFailure {
                _error.value = it.message ?: "Ошибка добавления товара"
                _isLoading.value = false
            }
        }
    }

    fun getLowInventory(threshold: Int = DEFAULT_LOW_STOCK_THRESHOLD) {
        selectedThreshold = threshold.coerceAtLeast(1)
        _isLoading.value = true
        viewModelScope.launch {
            refreshLowInventoryInternal()
            _isLoading.value = false
        }
    }

    private suspend fun refreshLowInventoryInternal() {
        if (selectedThreshold == DEFAULT_LOW_STOCK_THRESHOLD) {
            getLowInventoryUseCase().onSuccess { items ->
                _lowInventoryProducts.value = items.map { it.product }
                    .filter { it.quantity < selectedThreshold }
            }.onFailure {
                _lowInventoryProducts.value = _products.value.filter { it.quantity < selectedThreshold }
                _error.value = it.message ?: "Не удалось загрузить товары с низким остатком"
            }
            return
        }

        _lowInventoryProducts.value = _products.value.filter { it.quantity < selectedThreshold }
    }

    companion object {
        const val DEFAULT_LOW_STOCK_THRESHOLD = 5
    }
}