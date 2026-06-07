package com.example.client_kurs.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class OrderItemDto(
    val productId: String,
    val quantity: Int,
    val priceAtPurchase: String? = null
)

@Serializable
data class CreateOrderRequest(
    val items: List<OrderItemDto>
)

/** Ответ сервера при запросе истории заказов */
@Serializable
data class OrderDto(
    val id: String,
    val userId: String,
    val items: List<OrderItemDto>,
    val timestamp: Long,
    val totalPrice: String,
    val createdAt: String? = null
)
