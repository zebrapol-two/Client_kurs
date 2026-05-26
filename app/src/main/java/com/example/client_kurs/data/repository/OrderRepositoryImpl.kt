package com.example.client_kurs.data.repository

import com.example.client_kurs.data.remote.dto.CreateOrderRequest
import com.example.client_kurs.data.remote.dto.OrderDto
import com.example.client_kurs.data.remote.dto.OrderItemDto
import com.example.client_kurs.data.remote.getAuthToken
import com.example.client_kurs.data.remote.ktorClient
import com.example.client_kurs.domain.model.Order
import com.example.client_kurs.domain.model.OrderItem
import com.example.client_kurs.domain.repository.OrderRepository
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ORDER_DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale("ru", "RU"))
        .withZone(ZoneId.systemDefault())

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

    override suspend fun getMyOrders(): Result<List<Order>> {
        return try {
            val token = getAuthToken()
            val orders = ktorClient.get("/api/orders/my") {
                header("Authorization", "Bearer $token")
            }.body<List<OrderDto>>()
            Result.success(orders.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private fun OrderDto.toDomain() = Order(
    id = id,
    items = items.map { OrderItem(it.productId, it.quantity) },
    totalPrice = totalPrice.toDoubleOrNull() ?: 0.0,
    timestamp = ORDER_DATE_FORMATTER.format(Instant.ofEpochMilli(timestamp))
)
