package com.example.client_kurs.data.repository

import android.util.Log
import com.example.client_kurs.auth.FirebaseAuthManager
import com.example.client_kurs.data.local.UserPreferencesManager
import com.example.client_kurs.data.remote.RefreshTokenApi
import com.example.client_kurs.data.remote.dto.AuthResponse
import com.example.client_kurs.data.remote.dto.RegisterRequest
import com.example.client_kurs.domain.model.UserRole
import com.example.client_kurs.domain.repository.AuthRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.plugin
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess

class AuthRepositoryImpl(
    private val firebaseAuthManager: FirebaseAuthManager,
    private val userPreferencesManager: UserPreferencesManager,
    private val httpClient: HttpClient,
    private val authorizedHttpClient: HttpClient,
    private val refreshTokenApi: RefreshTokenApi
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepository"
    }
    // AuthRepositoryImpl.kt
    override suspend fun registerWithRole(email: String, password: String, role: String): Result<Unit> {
        return try {
            // Firebase-пользователь уже создан и залогинен на этапе register()
            val firebaseToken = firebaseAuthManager.getIdToken(forceRefresh = true)
                ?: return Result.failure(Exception("Не удалось получить Firebase токен"))

            val response = httpClient.post("/api/auth/register") {
                setBody(RegisterRequest(
                    firebaseToken = firebaseToken,
                    email = email,
                    role = role
                ))
                header("Content-Type", "application/json")
            }

            if (!response.status.isSuccess()) {
                val errorMsg = try { response.body<Map<String, String>>()["error"] } catch (_: Exception) { null }
                return Result.failure(Exception(errorMsg ?: "Ошибка регистрации на сервере"))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка регистрации с ролью", e)
            Result.failure(e)
        }
    }
    override suspend fun login(email: String, password: String): Result<UserRole> {
        return try {
            // 1. Вход в Firebase
            firebaseAuthManager.signIn(email, password)
            val firebaseToken = firebaseAuthManager.getIdToken(forceRefresh = true)
                ?: return Result.failure(Exception("Не удалось получить Firebase токен"))

            // 2. Отправляем Firebase токен на /api/auth/login
            val response = httpClient.post("/api/auth/login") {
                setBody(mapOf("firebaseToken" to firebaseToken))
                header("Content-Type", "application/json")
            }

            if (!response.status.isSuccess()) {
                val errorMsg = try { response.body<Map<String, String>>()["error"] } catch (_: Exception) { null }
                return Result.failure(Exception(errorMsg ?: "Ошибка входа на сервере"))
            }

            val authResponse = response.body<AuthResponse>()
            // 3. Сохраняем токены и роль
            userPreferencesManager.saveAccessToken(authResponse.accessToken)
            userPreferencesManager.saveRefreshToken(authResponse.refreshToken)
            val role = UserRole.valueOf(authResponse.role.uppercase())
            userPreferencesManager.saveRole(role)

            Log.d(TAG, "Login успешен, роль: $role")
            Result.success(role)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка логина", e)
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String): Result<Unit> {
        return try {
            // 1. Создаём только Firebase-аккаунт.
            //    Запись на сервере и роль создаются позже, после выбора на экране RoleSelectionScreen.
            firebaseAuthManager.signUp(email, password)

            // 2. Возвращаем успех, чтобы UI перевёл пользователя на экран выбора роли.
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка регистрации", e)
            Result.failure(e)
        }
    }

    override suspend fun saveRole(role: UserRole): Result<Unit> {
        // Роль уже установлена на сервере при регистрации, этот метод не нужен.
        // Если нужно менять роль – нужен отдельный эндпоинт.
        userPreferencesManager.saveRole(role)
        return Result.success(Unit)
    }

    override fun getRole(): UserRole? = userPreferencesManager.getRole()

    override fun isUserLoggedIn(): Boolean = userPreferencesManager.getAccessToken() != null

    override suspend fun logout() {
        val accessToken = userPreferencesManager.getAccessToken()

        try {
            if (!accessToken.isNullOrBlank()) {
                httpClient.post("/api/auth/logout") {
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                    header(HttpHeaders.ContentType, "application/json")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Logout API failed, clearing local session anyway", e)
        } finally {
            clearLocalSession()
        }
    }

    // Метод для обновления accessToken (используется в KtorClientFactory)
    override suspend fun refreshAccessToken(): String? {
        val refreshToken = userPreferencesManager.getRefreshToken() ?: return null
        val result = refreshTokenApi.refresh(refreshToken)
        if (result.isSuccess) {
            val tokens = result.getOrNull() ?: return null
            userPreferencesManager.saveAccessToken(tokens.accessToken)
            userPreferencesManager.saveRefreshToken(tokens.refreshToken)
            return tokens.accessToken
        } else {
            // Refresh не удался — очищаем сессию, чтобы не зациклить refresh-попытки.
            clearLocalSession()
            return null
        }
    }

    private fun clearLocalSession() {
        firebaseAuthManager.signOut()
        userPreferencesManager.clearAll()

        try {
            val authPlugin = authorizedHttpClient.plugin(Auth)
            authPlugin.providers
                .filterIsInstance<BearerAuthProvider>()
                .firstOrNull()
                ?.clearToken()
        } catch (e: Exception) {
            Log.w(TAG, "Unable to clear Ktor bearer cache", e)
        }
    }
}