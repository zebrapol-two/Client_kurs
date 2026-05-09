package com.example.client_kurs.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
<<<<<<< HEAD
    val id: String,
=======
<<<<<<< HEAD
    val firebaseToken: String,
=======
    val firebaseUid: String,
>>>>>>> 6d7f8b3 (Исправление логики отправки запросов на получение имеющихся товаров на складе)
>>>>>>> 8119b08 (Исправление логики отправки запросов на получение имеющихся товаров на складе)
    val email: String,
    val role: String
)

@Serializable
data class UserRoleResponse(
    val role: String
)

@Serializable
data class StockUpdateRequest(
    val newQuantity: Int
)
<<<<<<< HEAD
=======

@Serializable
data class PurchaseCreateRequest(
    val productId: String,
<<<<<<< HEAD
    val supplierId: String,
=======
    val supplierId: Int,
>>>>>>> 6d7f8b3 (Исправление логики отправки запросов на получение имеющихся товаров на складе)
    val quantity: Int
)

@Serializable
data class ReceiveGoodsRequest(
    val quantity: Int? = null
)

@Serializable
data class CreateExternalProductRequest(
    val code: String,
    val productName: String,
    val marketPrice: Double
)
>>>>>>> 8119b08 (Исправление логики отправки запросов на получение имеющихся товаров на складе)
