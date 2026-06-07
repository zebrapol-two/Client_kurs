package com.example.client_kurs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.client_kurs.domain.model.AnalyticsOverview
import com.example.client_kurs.domain.model.TopSellingProduct
import com.example.client_kurs.domain.repository.AnalyticsRepository
import com.example.client_kurs.domain.usecase.GetOverviewAnalyticsUseCase
import com.example.client_kurs.domain.usecase.GetTopSellingUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnalyticsViewModel(
    private val getOverviewAnalyticsUseCase: GetOverviewAnalyticsUseCase,
    private val getTopSellingUseCase: GetTopSellingUseCase,
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    private val _overview = MutableStateFlow<AnalyticsOverview?>(null)
    val overview = _overview.asStateFlow()

    private val _topSelling = MutableStateFlow<List<TopSellingProduct>>(emptyList())
    val topSelling = _topSelling.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // Выбранные пользователем товары (ID)
    private val _selectedProductIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedProductIds = _selectedProductIds.asStateFlow()

    // Сумма продаж за 7 дней для каждого выбранного товара
    private val _productSalesSum = MutableStateFlow<Map<String, Int>>(emptyMap())
    val productSalesSum = _productSalesSum.asStateFlow()

    // Кэш названий товаров из топ-5
    private var productNames = mapOf<String, String>()

    init {
        loadAnalytics()
    }

    fun loadAnalytics() {
        _isLoading.value = true
        viewModelScope.launch {
            getOverviewAnalyticsUseCase()
                .onSuccess { _overview.value = it }
                .onFailure { _error.value = it.message ?: "Не удалось загрузить обзор аналитики" }

            getTopSellingUseCase()
                .onSuccess { items ->
                    _topSelling.value = items
                    productNames = items.associate { it.productId to it.productName }
                }
                .onFailure { _error.value = it.message ?: "Не удалось загрузить топ продаж" }

            _isLoading.value = false
        }
    }

    /** Добавить или убрать товар из выборки */
    fun toggleProductSelection(productId: String) {
        val currentSet = _selectedProductIds.value
        if (currentSet.contains(productId)) {
            // Убираем товар и его данные
            _selectedProductIds.value = currentSet - productId
            _productSalesSum.value = _productSalesSum.value - productId
        } else {
            // Добавляем товар и загружаем его суммарные продажи за 7 дней
            _selectedProductIds.value = currentSet + productId
            loadProductTotalSold(productId)
        }
    }

    /** Сбросить все выбранные товары */
    fun clearSelection() {
        _selectedProductIds.value = emptySet()
        _productSalesSum.value = emptyMap()
    }

    private fun loadProductTotalSold(productId: String) {
        viewModelScope.launch {
            analyticsRepository.getProductSales(productId)
                .onSuccess { dailySales ->
                    val total = dailySales.sumOf { it.soldUnits }
                    _productSalesSum.value = _productSalesSum.value + mapOf(productId to total)
                }
                .onFailure {
                    _error.value = it.message ?: "Ошибка загрузки продаж товара"
                    // Если не удалось загрузить – всё равно оставляем товар выбранным,
                    // но суммы не будет (можно отобразить 0 или сообщение об ошибке)
                }
        }
    }

    fun clearError() {
        _error.value = null
    }
}