package com.example.client_kurs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.client_kurs.domain.model.RecommendedOrder
import com.example.client_kurs.domain.usecase.GetRecommendedOrderUseCase
import com.example.client_kurs.domain.usecase.UpdateProductStockUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecommendOrderViewModel(
    private val getRecommendedOrderUseCase: GetRecommendedOrderUseCase,
    private val updateProductStockUseCase: UpdateProductStockUseCase
) : ViewModel() {

    private val _recommendedOrders = MutableStateFlow<List<RecommendedOrder>>(emptyList())
    val recommendedOrders = _recommendedOrders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        loadRecommendations()
    }

    fun loadRecommendations() {
        _isLoading.value = true
        viewModelScope.launch {
            getRecommendedOrderUseCase()
                .onSuccess { _recommendedOrders.value = it }
                .onFailure { _error.value = it.message ?: "Не удалось рассчитать рекомендации" }
            _isLoading.value = false
        }
    }

    fun createSupplierOrder(item: RecommendedOrder) {
        if (item.recommendedOrder <= 0) {
            _error.value = "Рекомендованное количество должно быть больше 0"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            updateProductStockUseCase(item.productId, item.recommendedOrder)
                .onSuccess { loadRecommendations() }
                .onFailure {
                    _error.value = it.message ?: "Не удалось создать заказ поставщику"
                    _isLoading.value = false
                }
        }
    }

    fun clearError() {
        _error.value = null
    }
}