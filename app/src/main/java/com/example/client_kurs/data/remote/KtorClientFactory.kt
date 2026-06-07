package com.example.client_kurs.data.remote

import android.util.Log
import com.example.client_kurs.data.local.UserPreferencesManager
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
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

class KtorClientFactory(
    private val prefs: UserPreferencesManager,
    private val refreshTokenApi: RefreshTokenApi
) {
    private val refreshMutex = Mutex()
    private var isRefreshing = false

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
                // Не отправляем access token на refresh-запрос, чтобы не ловить рекурсию.
                sendWithoutRequest { request ->
                    !request.url.encodedPath.contains("/api/auth/refresh")
                }

                loadTokens {
                    val access = prefs.getAccessToken()
                    val refresh = prefs.getRefreshToken()
                    if (access.isNullOrBlank() || refresh.isNullOrBlank()) {
                        null
                    } else {
                        BearerTokens(access, refresh)
                    }
                }

                refreshTokens {
                    refreshMutex.withLock {
                        if (isRefreshing) {
                            val cachedAccess = prefs.getAccessToken()
                            val cachedRefresh = prefs.getRefreshToken()
                            return@refreshTokens if (cachedAccess.isNullOrBlank() || cachedRefresh.isNullOrBlank()) {
                                null
                            } else {
                                BearerTokens(cachedAccess, cachedRefresh)
                            }
                        }

                        isRefreshing = true
                        try {
                            val currentRefresh = prefs.getRefreshToken()
                            if (currentRefresh.isNullOrBlank()) {
                                prefs.clearAll()
                                return@refreshTokens null
                            }

                            val result = refreshTokenApi.refresh(currentRefresh)
                            if (result.isFailure) {
                                prefs.clearAll()
                                return@refreshTokens null
                            }

                            val tokens = result.getOrNull() ?: return@refreshTokens null
                            prefs.saveAccessToken(tokens.accessToken)
                            prefs.saveRefreshToken(tokens.refreshToken)
                            BearerTokens(tokens.accessToken, tokens.refreshToken)
                        } catch (e: Exception) {
                            Log.e("KtorClient", "refreshTokens failed", e)
                            prefs.clearAll()
                            null
                        } finally {
                            isRefreshing = false
                        }
                    }
                }
            }
        }
    }
}