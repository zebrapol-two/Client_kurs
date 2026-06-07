package com.example.client_kurs.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PurchaseOrderDto(
    val id: String,
    val productId: String,
    val productName: String,
    val supplierId: String,
    val supplierName: String,
    val orderedQuantity: Int,
    val receivedQuantity: Int,
    val status: String,
    val createdAt: Long
)