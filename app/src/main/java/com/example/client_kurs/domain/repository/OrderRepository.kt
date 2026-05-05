package com.example.client_kurs.domain.repository

import com.example.client_kurs.domain.model.OrderItem

interface OrderRepository {
    suspend fun createOrder(items: List<OrderItem>): Result<Unit>
}

