package com.example.client_kurs.data.repository

import com.example.client_kurs.auth.FirebaseAuthManager
import com.example.client_kurs.data.remote.dto.ProductDto
import com.example.client_kurs.data.remote.dto.StockUpdateRequest
<<<<<<< HEAD
import com.example.client_kurs.data.remote.getAuthToken
import com.example.client_kurs.data.remote.ktorClient
=======
<<<<<<< HEAD
=======
import com.example.client_kurs.data.remote.ktorClient
>>>>>>> 6d7f8b3 (Исправление логики отправки запросов на получение имеющихся товаров на складе)
>>>>>>> 8119b08 (Исправление логики отправки запросов на получение имеющихся товаров на складе)
import com.example.client_kurs.domain.model.Product
import com.example.client_kurs.domain.repository.ProductRepository
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

<<<<<<< HEAD
class ProductRepositoryImpl : ProductRepository {
    override suspend fun getAllProducts(): Result<List<Product>> {
        return try {
            val token = getAuthToken()
            val products = ktorClient.get("/api/products") {
                header("Authorization", "Bearer $token")
            }.body<List<ProductDto>>()
=======
class ProductRepositoryImpl(
<<<<<<< HEAD
    private val httpClient: HttpClient
) : ProductRepository {

    override suspend fun getProducts(): Result<List<Product>> {
        return try {
            val products = httpClient.get("/api/products").body<List<ProductDto>>()
=======
    private val authManager: FirebaseAuthManager  // добавить зависимость
) : ProductRepository {

    // вспомогательная suspend-функция для получения токена
    private suspend fun getToken(): String? = authManager.getIdToken()

    override suspend fun getProducts(): Result<List<Product>> {
        return try {
            val token = getToken() ?: return Result.failure(Exception("Нет токена"))
            val products = ktorClient.get("/api/products") {
                header("Authorization", "Bearer $token")
            }.body<List<ProductDto>>()
>>>>>>> 6d7f8b3 (Исправление логики отправки запросов на получение имеющихся товаров на складе)
>>>>>>> 8119b08 (Исправление логики отправки запросов на получение имеющихся товаров на складе)
            Result.success(products.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addProduct(name: String, price: Double, quantity: Int): Result<Product> {
        return try {
<<<<<<< HEAD
            val token = getAuthToken()
            val productDto = ProductDto(name = name, price = price, quantity = quantity)
            val response = ktorClient.post("/api/products") {
                header("Authorization", "Bearer $token")
=======
<<<<<<< HEAD
=======
            val token = getToken() ?: return Result.failure(Exception("Нет токена"))
>>>>>>> 6d7f8b3 (Исправление логики отправки запросов на получение имеющихся товаров на складе)
            val productDto = ProductDto(
                id = productId,
                name = name,
                price = price.toString(),
                quantity = quantity
            )
<<<<<<< HEAD
            val response = httpClient.post("/api/products") {
=======
            val response = ktorClient.post("/api/products") {
                header("Authorization", "Bearer $token")
>>>>>>> 6d7f8b3 (Исправление логики отправки запросов на получение имеющихся товаров на складе)
>>>>>>> 8119b08 (Исправление логики отправки запросов на получение имеющихся товаров на складе)
                setBody(productDto)
            }.body<ProductDto>()
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateQuantity(productId: Int, newQuantity: Int): Result<Unit> {
        return try {
<<<<<<< HEAD
            val token = getAuthToken()
            ktorClient.put("/api/products/$productId/stock") {
                header("Authorization", "Bearer $token")
                setBody(StockUpdateRequest(newQuantity))
=======
<<<<<<< HEAD
            httpClient.put("/api/products/$productId/stock") {
=======
            val token = getToken() ?: return Result.failure(Exception("Нет токена"))
            ktorClient.put("/api/products/$productId/stock") {
                header("Authorization", "Bearer $token")
>>>>>>> 6d7f8b3 (Исправление логики отправки запросов на получение имеющихся товаров на складе)
                setBody(StockUpdateRequest(delta))
>>>>>>> 8119b08 (Исправление логики отправки запросов на получение имеющихся товаров на складе)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

