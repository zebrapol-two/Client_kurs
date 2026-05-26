package com.example.client_kurs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.client_kurs.domain.model.Product
import com.example.client_kurs.domain.model.SupplierOffer
import com.example.client_kurs.domain.usecase.CreatePurchaseUseCase
import com.example.client_kurs.domain.usecase.GetProductsUseCase
import com.example.client_kurs.domain.usecase.GetSuppliersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SupplierComparisonViewModel(
    private val getProductsUseCase: GetProductsUseCase,
    private val getSuppliersUseCase: GetSuppliersUseCase,
    private val createPurchaseUseCase: CreatePurchaseUseCase
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct = _selectedProduct.asStateFlow()

    private val _supplierOffers = MutableStateFlow<List<SupplierOffer>>(emptyList())
    val supplierOffers = _supplierOffers.asStateFlow()

    private val _marketPrice = MutableStateFlow(0.0)
    val marketPrice = _marketPrice.asStateFlow()

    private val _selectedSupplier = MutableStateFlow<SupplierOffer?>(null)
    val selectedSupplier = _selectedSupplier.asStateFlow()

    private val _purchaseQuantityInput = MutableStateFlow("1")
    val purchaseQuantityInput = _purchaseQuantityInput.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage = _successMessage.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        _isLoading.value = true
        viewModelScope.launch {
            getProductsUseCase()
                .onSuccess { fetchedProducts ->
                    _products.value = fetchedProducts
                    val currentSelectedId = _selectedProduct.value?.id
                    val selected = fetchedProducts.firstOrNull { it.id == currentSelectedId }
                        ?: fetchedProducts.firstOrNull()

                    if (selected != null) {
                        applySelectedProduct(selected)
                        loadSuppliers(selected.id)
                    } else {
                        _selectedProduct.value = null
                        _supplierOffers.value = emptyList()
                        _isLoading.value = false
                    }
                }
                .onFailure {
                    _error.value = it.message ?: "Не удалось загрузить товары"
                    _isLoading.value = false
                }
        }
    }

    fun selectProduct(productId: String) {
        val product = _products.value.firstOrNull { it.id == productId }
        if (product == null) {
            _error.value = "Товар не найден"
            return
        }

        applySelectedProduct(product)
        loadSuppliers(productId)
    }

    fun openPurchaseDialog(supplierOffer: SupplierOffer) {
        _selectedSupplier.value = supplierOffer
        _purchaseQuantityInput.value = "1"
    }

    fun closePurchaseDialog() {
        _selectedSupplier.value = null
    }

    fun onPurchaseQuantityChange(value: String) {
        if (value.isNotEmpty() && value.any { !it.isDigit() }) return
        _purchaseQuantityInput.value = value
    }

    fun confirmPurchase() {
        val product = _selectedProduct.value ?: run {
            _error.value = "Сначала выберите товар"
            return
        }
        val supplier = _selectedSupplier.value ?: return
        val quantity = _purchaseQuantityInput.value.toIntOrNull()
        if (quantity == null || quantity <= 0) {
            _error.value = "Введите корректное количество"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            createPurchaseUseCase(product.id, supplier.supplierId, quantity)
                .onSuccess {
                    _successMessage.value = "Приход оформлен"
                    _selectedSupplier.value = null
                    loadProducts()
                }
                .onFailure {
                    _error.value = it.message ?: "Не удалось оформить приход"
                    _isLoading.value = false
                }
        }
    }

    private fun applySelectedProduct(product: Product) {
        _selectedProduct.value = product
        _marketPrice.value = product.price * 1.12
    }

    private fun loadSuppliers(productId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            getSuppliersUseCase(productId)
                .onSuccess { _supplierOffers.value = it }
                .onFailure {
                    _supplierOffers.value = emptyList()
                    _error.value = it.message ?: "Не удалось получить поставщиков"
                }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}