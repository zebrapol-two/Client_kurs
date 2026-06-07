package com.example.client_kurs.data.repository

import com.example.client_kurs.data.remote.dto.ProductDto
import com.example.client_kurs.data.remote.dto.StockUpdateRequest
import com.example.client_kurs.domain.model.Product
import com.example.client_kurs.domain.repository.ProductRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

class ProductRepositoryImpl(
    private val httpClient: HttpClient
) : ProductRepository {

    override suspend fun getProducts(): Result<List<Product>> {
        return try {
            val products = httpClient.get("/api/products").body<List<ProductDto>>()
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
            val productDto = ProductDto(
                id = productId,
                name = name,
                price = price.toString(),
                quantity = quantity
            )
            val response = httpClient.post("/api/products") {
                setBody(productDto)
            }.body<ProductDto>()
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateStock(productId: String, delta: Int): Result<Unit> {
        return try {
            httpClient.put("/api/products/$productId/stock") {
                setBody(StockUpdateRequest(delta))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}