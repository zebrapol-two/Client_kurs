package com.example.client_kurs.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess

class RefreshTokenApi(
    private val client: HttpClient
) {
    suspend fun refresh(refreshToken: String): Result<String> {
        return try {
            val response = client.post("/api/auth/refresh") {
                setBody(mapOf("refreshToken" to refreshToken))
            }
            if (response.status.isSuccess()) {
                val body = response.body<Map<String, String>>()
                val newToken = body["accessToken"]
                if (newToken != null) Result.success(newToken)
                else Result.failure(Exception("No accessToken in refresh response"))
            } else {
                Result.failure(Exception("Refresh failed: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}