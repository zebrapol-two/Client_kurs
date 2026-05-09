package com.example.client_kurs.data.remote.api

import com.example.client_kurs.data.remote.dto.CreateOrderRequest
import com.example.client_kurs.data.remote.dto.FakeStoreProductDto
import com.example.client_kurs.data.remote.dto.OrderDto
import com.example.client_kurs.data.remote.dto.ProductDto
import com.example.client_kurs.data.remote.dto.PurchaseOrderDto
import com.example.client_kurs.data.remote.dto.ReceiveGoodsRequest
import com.example.client_kurs.data.remote.dto.RegisterRequest
import com.example.client_kurs.data.remote.dto.StockUpdateRequest
import com.example.client_kurs.data.remote.dto.UserRoleResponse
import com.example.client_kurs.domain.model.AnalyticsOverview
import com.example.client_kurs.domain.model.SupplierOffer
import com.example.client_kurs.domain.model.TopSellingProduct

<<<<<<< HEAD
interface KtorApiService {

    suspend fun getProducts(): List<ProductDto>

    suspend fun searchProducts(query: String): List<FakeStoreProductDto>

    suspend fun addProduct(dto: ProductDto): ProductDto

    suspend fun updateStock(productId: String, request: StockUpdateRequest)

    suspend fun getLowInventory(): List<ProductDto>

    suspend fun createOrder(request: CreateOrderRequest)

    suspend fun getOrders(): List<OrderDto>

    suspend fun getUserRole(userId: String): UserRoleResponse

    suspend fun registerUser(request: RegisterRequest)

    suspend fun getPendingPurchases(): List<PurchaseOrderDto>

    suspend fun receiveGoods(purchaseId: String, request: ReceiveGoodsRequest)

    suspend fun getAnalyticsOverview(): AnalyticsOverview

    suspend fun getTopSelling(): List<TopSellingProduct>

    suspend fun createExternalProduct(code: String, productName: String, marketPrice: Double): Result<Unit>

    suspend fun getExternalSuppliers(code: String, marketPrice: Double): Result<List<SupplierOffer>>
}
=======
/** Преобразует [ProductDto] (ответ API) в доменную модель [Product]. */
fun ProductDto.toDomainProduct() = Product(
    id = id ?: throw IllegalArgumentException("Product id must not be null"),
    name = name,
    price = price.toDoubleOrNull() ?: 0.0,
    quantity = quantity
)

/** Преобразует доменную модель [Product] в [ProductDto] для отправки на сервер. */
fun Product.toProductDto() = ProductDto(
    id = id,
    name = name,
    price = price.toString(),
    quantity = quantity
)
>>>>>>> 6d7f8b3 (Исправление логики отправки запросов на получение имеющихся товаров на складе)
