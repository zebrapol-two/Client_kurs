package com.example.client_kurs.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
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

