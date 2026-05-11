package com.example.client_kurs.data.remote.api

import com.example.client_kurs.data.remote.dto.CreateOrderRequest
import com.example.client_kurs.data.remote.dto.OrderDto
import com.example.client_kurs.data.remote.dto.ProductDto
import com.example.client_kurs.data.remote.dto.RegisterRequest
import com.example.client_kurs.data.remote.dto.StockUpdateRequest
import com.example.client_kurs.data.remote.dto.UserRoleResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

class KtorApiServiceImpl(private val httpClient: HttpClient) : KtorApiService {

    override suspend fun getProducts(): List<ProductDto> =
        httpClient.get("/api/products").body()

    override suspend fun addProduct(dto: ProductDto): ProductDto =
        httpClient.post("/api/products") { setBody(dto) }.body()

    override suspend fun updateStock(productId: String, request: StockUpdateRequest) {
        httpClient.put("/api/products/$productId/stock") { setBody(request) }
    }

    override suspend fun getLowInventory(): List<ProductDto> =
        httpClient.get("/api/inventory/low").body()

    override suspend fun createOrder(request: CreateOrderRequest) {
        httpClient.post("/api/order/create") { setBody(request) }
    }

    override suspend fun getOrders(): List<OrderDto> =
        httpClient.get("/api/orders/my").body()

    override suspend fun getUserRole(userId: String): UserRoleResponse =
        httpClient.get("/api/auth/role/$userId").body()

    override suspend fun registerUser(request: RegisterRequest) {
        httpClient.post("/api/auth/register") { setBody(request) }
    }
}
