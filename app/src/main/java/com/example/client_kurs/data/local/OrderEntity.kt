package com.example.client_kurs.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey
    val id: String,
    val itemsJson: String,    // JSON-строка списка OrderItem
    val totalPrice: Double,
    val timestamp: String
)