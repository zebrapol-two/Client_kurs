package com.example.client_kurs.data.remote

import android.util.Log
import com.example.client_kurs.data.local.UserPreferencesManager
import com.example.client_kurs.domain.repository.AuthRepository
import com.example.client_kurs.utils.ServerConfig
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

class KtorClientFactory(
    private val prefs: UserPreferencesManager,
    private val authRepository: AuthRepository
) {
    private val mutex = Mutex()
    private var refreshing = false

    fun create(): HttpClient = HttpClient(Android) {
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = 30_000
        }

        install(Logging) {
            logger = object : io.ktor.client.plugins.logging.Logger {
                override fun log(message: String) {
                    Log.d("KtorClient", message)
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

        defaultRequest {
            url(ServerConfig.BASE_URL)
            contentType(ContentType.Application.Json)
        }

        install(Auth) {
            bearer {
                loadTokens {
                    val token = prefs.getAccessToken()
                    Log.d("KtorClient", "loadTokens: token = ${token?.take(20)}...")
                    if (token != null) BearerTokens(token, "") else null
                }
                refreshTokens {
                    mutex.withLock {
                        if (refreshing) {
                            return@refreshTokens BearerTokens(prefs.getAccessToken() ?: "", "")
                        }
                        refreshing = true
                        try {
                            val newToken = authRepository.refreshAccessToken()
                            Log.d("KtorClient", "refreshTokens: newToken = ${newToken?.take(20)}...")
                            if (newToken != null) {
                                BearerTokens(newToken, "")
                            } else {
                                null
                            }
                        } finally {
                            refreshing = false
                        }
                    }
                }
            }
        }
    }
}