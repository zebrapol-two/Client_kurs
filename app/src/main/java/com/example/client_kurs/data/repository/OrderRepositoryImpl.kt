package com.example.client_kurs.data.repository

import com.example.client_kurs.data.local.AppDatabase
import com.example.client_kurs.data.local.OrderEntity
import com.example.client_kurs.data.remote.dto.CreateOrderRequest
import com.example.client_kurs.data.remote.dto.OrderDto
import com.example.client_kurs.data.remote.dto.OrderItemDto
import com.example.client_kurs.domain.model.Order
import com.example.client_kurs.domain.model.OrderItem
import com.example.client_kurs.domain.repository.OrderRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ORDER_DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale("ru", "RU"))
        .withZone(ZoneId.systemDefault())

class OrderRepositoryImpl(
    private val httpClient: HttpClient,
    private val database: AppDatabase
) : OrderRepository {

    private val orderDao = database.orderDao()

    override suspend fun createOrder(items: List<OrderItem>): Result<Unit> {
        return try {
            val request = CreateOrderRequest(
                items = items.map { OrderItemDto(it.productId, it.quantity) }
            )
            httpClient.post("/api/order/create") {
                setBody(request)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMyOrders(): Result<List<Order>> {
        return try {
            val remoteOrders = httpClient.get("/api/orders/my").body<List<OrderDto>>()
            val domainOrders = remoteOrders.map { it.toDomain() }
            // Сохраняем в кэш
            saveOrdersToCache(domainOrders)
            Result.success(domainOrders)
        } catch (e: Exception) {
            // При ошибке сети или сервера – берём из кэша
            val cached = loadOrdersFromCache()
            if (cached.isNotEmpty()) {
                Result.success(cached)
            } else {
                Result.failure(e)
            }
        }
    }

    // Поток заказов для ViewModel (cache-first)
    fun observeOrders(): Flow<List<Order>> {
        return orderDao.getAllOrders().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    private suspend fun saveOrdersToCache(orders: List<Order>) {
        val entities = orders.map { order ->
            val itemsJson = Json.encodeToString(order.items)
            OrderEntity(
                id = order.id,
                itemsJson = itemsJson,
                totalPrice = order.totalPrice,
                timestamp = order.timestamp
            )
        }
        orderDao.insertAll(entities)
    }

    private suspend fun loadOrdersFromCache(): List<Order> {
        return orderDao.getAllOrders().firstOrNull()?.map { it.toDomain() } ?: emptyList()
    }
}

private fun OrderDto.toDomain() = Order(
    id = id,
    items = items.map { OrderItem(it.productId, it.quantity) },
    totalPrice = totalPrice.toDoubleOrNull() ?: 0.0,
    timestamp = ORDER_DATE_FORMATTER.format(Instant.ofEpochMilli(timestamp))
)

private fun OrderEntity.toDomain(): Order {
    val items = try {
        Json.decodeFromString<List<OrderItem>>(itemsJson)
    } catch (e: Exception) {
        emptyList()
    }
    return Order(
        id = id,
        items = items,
        totalPrice = totalPrice,
        timestamp = timestamp
    )
}