package com.example.client_kurs.data.remote

import android.util.Log
import com.example.client_kurs.utils.ServerConfig
import com.google.firebase.auth.FirebaseAuth
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json

/**
 * Фабрика для создания [HttpClient].
 * Устанавливает ContentNegotiation, Logging, HttpTimeout и плагин Auth,
 * который автоматически добавляет заголовок `Authorization: Bearer <token>`
 * к каждому запросу, получая токен из FirebaseAuth.
 */
object KtorClientFactory {

    private const val TAG = "KtorClientFactory"

    fun create(): HttpClient = HttpClient(Android) {
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = 30_000
        }

        install(Logging) {
            logger = object : io.ktor.client.plugins.logging.Logger {
                override fun log(message: String) {
                    Log.d(TAG, message)
                }
            }
            level = LogLevel.BODY
        }

        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        install(Auth) {
            bearer {
                loadTokens {
                    fetchBearerTokens()
                }
                refreshTokens {
                    fetchBearerTokens()
                }
            }
        }

        defaultRequest {
            url(ServerConfig.BASE_URL)
            contentType(ContentType.Application.Json)
        }
    }

    private suspend fun fetchBearerTokens(): BearerTokens? {
        return try {
            val token = FirebaseAuth.getInstance().currentUser
                ?.getIdToken(false)
                ?.await()
                ?.token
            if (token != null) BearerTokens(accessToken = token, refreshToken = "") else null
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка получения Firebase токена: ${e.message}")
            null
        }
    }
}
