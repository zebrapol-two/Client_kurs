package com.example.client_kurs.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class OrderItemDto(
    val productId: Int,
    val quantity: Int
)

@Serializable
data class CreateOrderRequest(
    val items: List<OrderItemDto>
)

