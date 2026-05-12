package com.example.client_kurs.data.remote.api

import com.example.client_kurs.data.remote.dto.CreateOrderRequest
import com.example.client_kurs.data.remote.dto.OrderDto
import com.example.client_kurs.data.remote.dto.ProductDto
import com.example.client_kurs.data.remote.dto.PurchaseOrderDto
import com.example.client_kurs.data.remote.dto.ReceiveGoodsRequest
import com.example.client_kurs.data.remote.dto.RegisterRequest
import com.example.client_kurs.data.remote.dto.StockUpdateRequest
import com.example.client_kurs.data.remote.dto.UserRoleResponse

interface KtorApiService {

    /** Получить список всех товаров */
    suspend fun getProducts(): List<ProductDto>

    /** Добавить новый товар (только кладовщик) */
    suspend fun addProduct(dto: ProductDto): ProductDto

    /** Обновить количество товара на складе */
    suspend fun updateStock(productId: String, request: StockUpdateRequest)

    /** Получить товары с низким остатком */
    suspend fun getLowInventory(): List<ProductDto>

    /** Создать заказ (покупатель) */
    suspend fun createOrder(request: CreateOrderRequest)

    /** Получить историю заказов текущего пользователя */
    suspend fun getOrders(): List<OrderDto>

    /** Получить роль пользователя по его UID */
    suspend fun getUserRole(userId: String): UserRoleResponse

    /** Зарегистрировать пользователя на сервере */
    suspend fun registerUser(request: RegisterRequest)

    suspend fun getPendingPurchases(): List<PurchaseOrderDto>

    suspend fun receiveGoods(purchaseId: String, request: ReceiveGoodsRequest)
}
