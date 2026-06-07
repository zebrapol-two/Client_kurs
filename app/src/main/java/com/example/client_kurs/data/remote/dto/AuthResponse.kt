package com.example.client_kurs.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val role: String
)