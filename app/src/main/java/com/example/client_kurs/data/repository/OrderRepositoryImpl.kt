package com.example.client_kurs.data.repository

import com.example.client_kurs.data.remote.dto.CreateOrderRequest
import com.example.client_kurs.data.remote.dto.OrderItemDto
import com.example.client_kurs.data.remote.getAuthToken
import com.example.client_kurs.data.remote.ktorClient
import com.example.client_kurs.domain.model.OrderItem
import com.example.client_kurs.domain.repository.OrderRepository
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class OrderRepositoryImpl : OrderRepository {
    override suspend fun createOrder(items: List<OrderItem>): Result<Unit> {
        return try {
            val token = getAuthToken()
            val request = CreateOrderRequest(
                items = items.map { OrderItemDto(it.productId, it.quantity) }
            )
            ktorClient.post("/api/order/create") {
                header("Authorization", "Bearer $token")
                setBody(request)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

