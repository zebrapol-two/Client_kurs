package com.example.client_kurs.domain.model

data class InventoryItem(
    val product: Product,
    val expectedQuantity: Int,
    val actualQuantity: Int
)