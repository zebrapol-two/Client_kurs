package com.example.client_kurs.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess

class RefreshTokenApi(
    private val client: HttpClient
) {
        return try {
            val response = client.post("/api/auth/refresh") {
                setBody(mapOf("refreshToken" to refreshToken))
            }
            if (response.status.isSuccess()) {
            } else {
                Result.failure(Exception("Refresh failed: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}