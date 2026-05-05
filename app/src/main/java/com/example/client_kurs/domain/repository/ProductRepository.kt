package com.example.client_kurs.domain.repository

import com.example.client_kurs.domain.model.Product

interface ProductRepository {
    suspend fun getAllProducts(): Result<List<Product>>
    suspend fun addProduct(name: String, price: Double, quantity: Int): Result<Product>
    suspend fun updateQuantity(productId: Int, newQuantity: Int): Result<Unit>
}

