package com.example.client_kurs.data.remote.api

import com.example.client_kurs.data.remote.dto.*
import com.example.client_kurs.domain.model.AnalyticsOverview
import com.example.client_kurs.domain.model.SupplierOffer
import com.example.client_kurs.domain.model.TopSellingProduct
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
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

    override suspend fun getProducts(): List<ProductDto> {
        val response = httpClient.get("/api/products")
        return safeResponse(response)
    }

    override suspend fun searchProducts(query: String): List<FakeStoreProductDto> {
        val response = httpClient.get("/api/external/search") {
            parameter("query", query)
            parameter("pageSize", 20)
        }
        return safeResponse(response)
    }

    override suspend fun addProduct(dto: ProductDto): ProductDto =
        httpClient.post("/api/products") { setBody(dto) }.body()

    override suspend fun updateStock(productId: String, request: StockUpdateRequest) {
        httpClient.put("/api/products/$productId/stock") { setBody(request) }
    }

    override suspend fun getLowInventory(): List<ProductDto> {
        val response = httpClient.get("/api/inventory/low")
        return safeResponse(response)
    }

    override suspend fun createOrder(request: CreateOrderRequest) {
        httpClient.post("/api/order/create") { setBody(request) }
    }

    override suspend fun getOrders(): List<OrderDto> {
        val response = httpClient.get("/api/orders/my")
        return safeResponse(response)
    }

    override suspend fun getUserRole(userId: String): UserRoleResponse {
        val response = httpClient.get("/api/auth/role/$userId")
        return safeResponse(response)
    }

    override suspend fun registerUser(request: RegisterRequest) {
        httpClient.post("/api/auth/register") { setBody(request) }
    }

    override suspend fun getPendingPurchases(): List<PurchaseOrderDto> {
        val response = httpClient.get("/api/purchases/pending")
        return safeResponse(response)
    }

    override suspend fun receiveGoods(purchaseId: String, request: ReceiveGoodsRequest) {
        val response = httpClient.put("/api/purchases/$purchaseId/receive") { setBody(request) }
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

    override suspend fun createExternalProduct(code: String, productName: String, marketPrice: Double): Result<Unit> {
        return try {
            val request = CreateExternalProductRequest(code, productName, marketPrice)
            val response = httpClient.post("/api/products/external") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                val error = try {
                    response.body<Map<String, String>>()["error"] ?: "Failed to create product"
                } catch (e: Exception) {
                    "Failed to create product"
                }
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getExternalSuppliers(code: String, marketPrice: Double): Result<List<SupplierOffer>> {
        return try {
            val response = httpClient.get("/api/external/suppliers/$code") {
                parameter("marketPrice", marketPrice)
            }
            if (response.status.isSuccess()) {
                val dtos = response.body<List<ExternalSupplierDto>>()
                val offers = dtos.map { dto ->
                    SupplierOffer(dto.supplierId, dto.supplierName, dto.price)
                }
                Result.success(offers)
            } else {
                val error = try {
                    response.body<Map<String, String>>()["error"] ?: "Failed to get external suppliers"
                } catch (e: Exception) {
                    "Failed to get external suppliers"
                }
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}