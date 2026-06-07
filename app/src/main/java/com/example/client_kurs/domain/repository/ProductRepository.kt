package com.example.client_kurs.domain.repository

import com.example.client_kurs.domain.model.Product

interface ProductRepository {
    suspend fun getProducts(): Result<List<Product>>
    suspend fun addProduct(productId: String, name: String, price: Double, quantity: Int): Result<Product>
    suspend fun updateStock(productId: String, delta: Int): Result<Unit>
}