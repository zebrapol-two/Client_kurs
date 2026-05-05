package com.example.client_kurs.data.repository

import com.example.client_kurs.data.remote.dto.ProductDto
import com.example.client_kurs.data.remote.dto.StockUpdateRequest
import com.example.client_kurs.data.remote.getAuthToken
import com.example.client_kurs.data.remote.ktorClient
import com.example.client_kurs.domain.model.Product
import com.example.client_kurs.domain.repository.ProductRepository
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

class ProductRepositoryImpl : ProductRepository {
    override suspend fun getAllProducts(): Result<List<Product>> {
        return try {
            val token = getAuthToken()
            val products = ktorClient.get("/api/products") {
                header("Authorization", "Bearer $token")
            }.body<List<ProductDto>>()
            Result.success(products.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addProduct(name: String, price: Double, quantity: Int): Result<Product> {
        return try {
            val token = getAuthToken()
            val productDto = ProductDto(name = name, price = price, quantity = quantity)
            val response = ktorClient.post("/api/products") {
                header("Authorization", "Bearer $token")
                setBody(productDto)
            }.body<ProductDto>()
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateQuantity(productId: Int, newQuantity: Int): Result<Unit> {
        return try {
            val token = getAuthToken()
            ktorClient.put("/api/products/$productId/stock") {
                header("Authorization", "Bearer $token")
                setBody(StockUpdateRequest(newQuantity))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

