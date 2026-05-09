package com.example.client_kurs.domain.repository

import com.example.client_kurs.domain.model.Product

interface ProductRepository {
<<<<<<< HEAD
    suspend fun getAllProducts(): Result<List<Product>>
    suspend fun addProduct(name: String, price: Double, quantity: Int): Result<Product>
    suspend fun updateQuantity(productId: Int, newQuantity: Int): Result<Unit>
<<<<<<< HEAD
=======
    suspend fun getLowInventory(): Result<List<Product>>
=======
    suspend fun getProducts(): Result<List<Product>>
    suspend fun addProduct(productId: String, name: String, price: Double, quantity: Int): Result<Product>
    suspend fun updateStock(productId: String, delta: Int): Result<Unit>
>>>>>>> 6d7f8b3 (Исправление логики отправки запросов на получение имеющихся товаров на складе)
>>>>>>> 8119b08 (Исправление логики отправки запросов на получение имеющихся товаров на складе)
}

