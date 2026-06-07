package com.example.client_kurs.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable

@Serializable
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String
)

class RefreshTokenApi(
    private val client: HttpClient
) {
    suspend fun refresh(refreshToken: String): Result<AuthTokens> {
        return try {
            val response = client.post("/api/auth/refresh") {
                setBody(mapOf("refreshToken" to refreshToken))
            }
            if (response.status.isSuccess()) {
                Result.success(response.body<AuthTokens>())
            } else {
                Result.failure(Exception("Refresh failed: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}