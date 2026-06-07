package com.example.client_kurs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.client_kurs.data.remote.api.KtorApiService
import com.example.client_kurs.data.remote.dto.PurchaseOrderDto
import com.example.client_kurs.data.remote.dto.ReceiveGoodsRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PendingDeliveriesViewModel(
    private val purchaseApi: KtorApiService
) : ViewModel() {

    private val _deliveries = MutableStateFlow<List<PurchaseOrderDto>>(emptyList())
    val deliveries = _deliveries.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init { loadDeliveries() }

    fun loadDeliveries() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _deliveries.value = purchaseApi.getPendingPurchases()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun receiveAll(deliveryId: String) {
        viewModelScope.launch {
            try {
                purchaseApi.receiveGoods(deliveryId, ReceiveGoodsRequest(null))
                loadDeliveries()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun receivePartial(deliveryId: String, quantity: Int) {
        viewModelScope.launch {
            try {
                purchaseApi.receiveGoods(deliveryId, ReceiveGoodsRequest(quantity))
                loadDeliveries()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun clearError() { _error.value = null }
}