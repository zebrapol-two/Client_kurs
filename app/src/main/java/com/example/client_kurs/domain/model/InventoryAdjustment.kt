package com.example.client_kurs.domain.model

data class InventoryAdjustment(
    val productId: String,
    val productName: String,
    val expectedQuantity: Int,
    val actualQuantity: Int,
    val price: Double
) {
    val quantityDifference: Int
        get() = actualQuantity - expectedQuantity

    val amountDifference: Double
        get() = quantityDifference * price
}