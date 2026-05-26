package com.example.client_kurs.data.remote.api

import com.example.client_kurs.data.remote.dto.CreateOrderRequest
import com.example.client_kurs.data.remote.dto.OrderDto
import com.example.client_kurs.data.remote.dto.ProductDto
import com.example.client_kurs.data.remote.dto.PurchaseOrderDto
import com.example.client_kurs.data.remote.dto.ReceiveGoodsRequest
import com.example.client_kurs.data.remote.dto.RegisterRequest
import com.example.client_kurs.data.remote.dto.StockUpdateRequest
import com.example.client_kurs.data.remote.dto.UserRoleResponse
import com.example.client_kurs.domain.model.AnalyticsOverview
import com.example.client_kurs.domain.model.TopSellingProduct
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

class KtorApiServiceImpl(private val httpClient: HttpClient) : KtorApiService {

    private suspend inline fun <reified T> safeResponse(response: HttpResponse): T {
        if (!response.status.isSuccess()) {
            val errorBody = try {
                response.body<Map<String, String>>()
            } catch (e: Exception) {
                mapOf("error" to "Ошибка сервера (${response.status.value})")
            }
            throw Exception(errorBody["error"] ?: "Неизвестная ошибка")
        }
        return response.body()
    }

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
    override suspend fun getPendingPurchases(): List<PurchaseOrderDto> {
        val response = httpClient.get("/api/purchases/pending")
        return safeResponse(response)  // используй safeResponse из предыдущих советов
    }

    override suspend fun receiveGoods(purchaseId: String, request: ReceiveGoodsRequest) {
        val response = httpClient.put("/api/purchases/$purchaseId/receive") {
            setBody(request)
        }
        if (!response.status.isSuccess()) {
            val error = response.body<Map<String, String>>()
            throw Exception(error["error"] ?: "Ошибка приёмки")
        }
    }
    override suspend fun getAnalyticsOverview(): AnalyticsOverview {
        val response = httpClient.get("/api/analytics/overview")
        return safeResponse(response)
    }

    override suspend fun getTopSelling(): List<TopSellingProduct> {
        val response = httpClient.get("/api/analytics/top-selling")
        return safeResponse(response)
    }
}
