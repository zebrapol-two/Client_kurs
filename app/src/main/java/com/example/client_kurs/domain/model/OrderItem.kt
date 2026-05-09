package com.example.client_kurs.domain.model

<<<<<<< HEAD
=======
<<<<<<< HEAD
import kotlinx.serialization.Serializable
=======
data class OrderItem(
    val productId: String,
    val quantity: Int
)
>>>>>>> 6d7f8b3 (Исправление логики отправки запросов на получение имеющихся товаров на складе)

@Serializable
>>>>>>> 8119b08 (Исправление логики отправки запросов на получение имеющихся товаров на складе)
data class OrderItem(
    val productId: Int,
    val quantity: Int
)

