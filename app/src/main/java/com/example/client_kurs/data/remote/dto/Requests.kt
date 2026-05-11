package com.example.client_kurs.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val firebaseUid: String,
    val email: String,
    val role: String
)

@Serializable
data class UserRoleResponse(
    val role: String
)

@Serializable
data class StockUpdateRequest(
    val delta: Int
)

@Serializable
data class PurchaseCreateRequest(
    val productId: String,
    val supplierId: Int,
    val quantity: Int
)
