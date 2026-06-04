package com.example.client_kurs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.client_kurs.domain.model.InventoryAdjustment
import com.example.client_kurs.domain.model.Product
import com.example.client_kurs.domain.usecase.FinishInventoryUseCase
import com.example.client_kurs.domain.usecase.GetProductsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InventoryViewModel(
    private val getProductsUseCase: GetProductsUseCase,
    private val finishInventoryUseCase: FinishInventoryUseCase
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    private val _actualInputs = MutableStateFlow<Map<String, String>>(emptyMap())
    val actualInputs = _actualInputs.asStateFlow()

    private val _searchIdInput = MutableStateFlow("")
    val searchIdInput = _searchIdInput.asStateFlow()

    private val _highlightedProductId = MutableStateFlow<String?>(null)
    val highlightedProductId = _highlightedProductId.asStateFlow()

    private val _discrepancies = MutableStateFlow<List<InventoryAdjustment>>(emptyList())
    val discrepancies = _discrepancies.asStateFlow()

    private val _showReportDialog = MutableStateFlow(false)
    val showReportDialog = _showReportDialog.asStateFlow()

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
                .onSuccess { _products.value = it }
                .onFailure { _error.value = it.message ?: "Не удалось загрузить товары" }
            _isLoading.value = false
        }
    }

    fun onActualInputChange(productId: String, value: String) {
        if (value.isNotEmpty() && value.any { !it.isDigit() }) return
        _actualInputs.value = _actualInputs.value.toMutableMap().apply { put(productId, value) }
    }

    fun onSearchIdInputChange(value: String) {
        // Разрешаем любой ввод, так как ID теперь строка
        _searchIdInput.value = value
    }

    fun findById() {
        val searchId = _searchIdInput.value.trim()
        if (searchId.isBlank()) {
            _error.value = "Введите ID товара"
            return
        }
        _highlightedProductId.value = _products.value.firstOrNull { it.id == searchId }?.id
        if (_highlightedProductId.value == null) {
            _error.value = "Товар с ID $searchId не найден"
        }
    }

    fun prepareFinish() {
        val entered = _actualInputs.value
        val result = _products.value.mapNotNull { product ->
            val actual = entered[product.id]?.toIntOrNull() ?: return@mapNotNull null
            if (actual == product.quantity) return@mapNotNull null
            InventoryAdjustment(
                productId = product.id,
                productName = product.name,
                expectedQuantity = product.quantity,
                actualQuantity = actual,
                price = product.price
            )
        }

        _discrepancies.value = result
        _showReportDialog.value = true
    }

    fun dismissReportDialog() {
        _showReportDialog.value = false
    }

    fun confirmFinish() {
        _isLoading.value = true
        viewModelScope.launch {
            finishInventoryUseCase(_discrepancies.value)
                .onSuccess {
                    _successMessage.value = "Инвентаризация завершена"
                    _actualInputs.value = emptyMap()
                    _searchIdInput.value = ""
                    _highlightedProductId.value = null
                    _showReportDialog.value = false
                    loadProducts()
                }
                .onFailure {
                    _error.value = it.message ?: "Не удалось завершить инвентаризацию"
                    _isLoading.value = false
                }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}