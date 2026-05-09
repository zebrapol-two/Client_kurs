package com.example.client_kurs.domain.model

data class Order(
    val id: String,
    val items: List<OrderItem>,
    val totalPrice: Double,
    val timestamp: String
)
