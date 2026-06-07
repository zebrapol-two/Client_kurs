package com.example.client_kurs.data.remote

import com.example.client_kurs.utils.ServerConfig
import com.google.firebase.auth.FirebaseAuth
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.tasks.await
import io.ktor.client.plugins.HttpTimeout
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import android.util.Log

val ktorClient = HttpClient(Android) {
    install(HttpTimeout) {
        requestTimeoutMillis = 30000
        connectTimeoutMillis = 30000
        socketTimeoutMillis = 30000
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
}

suspend fun getAuthToken(): String? {
    val user = FirebaseAuth.getInstance().currentUser
    return try {
        user?.getIdToken(false)?.await()?.token
    } catch (_: Exception) {
        null
    }
}