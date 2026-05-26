package com.example.client_kurs.data.repository

import com.example.client_kurs.auth.FirebaseAuthManager
import com.example.client_kurs.data.remote.dto.ProductDto
import com.example.client_kurs.data.remote.dto.StockUpdateRequest
import com.example.client_kurs.data.remote.ktorClient
import com.example.client_kurs.domain.model.Product
import com.example.client_kurs.domain.repository.ProductRepository
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

class ProductRepositoryImpl(
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
            Result.success(products.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addProduct(
        productId: String,
        name: String,
        price: Double,
        quantity: Int
    ): Result<Product> {
        return try {
            val token = getToken() ?: return Result.failure(Exception("Нет токена"))
            val productDto = ProductDto(
                id = productId,
                name = name,
                price = price.toString(),
                quantity = quantity
            )
            val response = ktorClient.post("/api/products") {
                header("Authorization", "Bearer $token")
                setBody(productDto)
            }.body<ProductDto>()
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateStock(productId: String, delta: Int): Result<Unit> {
        return try {
            val token = getToken() ?: return Result.failure(Exception("Нет токена"))
            ktorClient.put("/api/products/$productId/stock") {
                header("Authorization", "Bearer $token")
                setBody(StockUpdateRequest(delta))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}