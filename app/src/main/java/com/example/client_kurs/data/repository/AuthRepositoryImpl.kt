package com.example.client_kurs.data.repository

import com.example.client_kurs.data.remote.getAuthToken
import com.example.client_kurs.data.remote.ktorClient
import com.example.client_kurs.data.remote.dto.RegisterRequest
import com.example.client_kurs.data.remote.dto.UserRoleResponse
import com.example.client_kurs.domain.model.UserRole
import com.example.client_kurs.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import android.util.Log
import io.ktor.client.request.setBody
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepository"
    }

    // We cache the current role in memory for quick access (or we could fetch it every time)
    private var cachedRole: UserRole? = null



    override suspend fun login(email: String, password: String): Result<UserRole> {
        return try {
            Log.d(TAG, "Попытка логина: $email")
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val token = getAuthToken()
            val userId = firebaseAuth.currentUser?.uid

            Log.d(TAG, "Firebase токен получен: ${token?.take(20)}...")
            Log.d(TAG, "Firebase UID: $userId")

            if (token.isNullOrEmpty()) {
                Log.e(TAG, "Токен Firebase пуст")
                return Result.failure(Exception("Не удалось получить токен Firebase"))
            }

            if (userId.isNullOrEmpty()) {
                Log.e(TAG, "UID пользователя пуст")
                return Result.failure(Exception("Не удалось получить UID пользователя"))
            }

            // Пытаемся получить роль с сервера
            try {
                Log.d(TAG, "Запрос на /api/auth/role/$userId")
                val response = ktorClient.get("/api/auth/role/$userId") {
                    header("Authorization", "Bearer $token")
                }
                Log.d(TAG, "Ответ сервера статус: ${response.status.value}")

                if (response.status.value == 404) {
                    Log.e(TAG, "Пользователь не найден на сервере (404)")
                    try {
                        val errorBody = response.bodyAsText()
                        Log.e(TAG, "Тело ответа 404: $errorBody")
                    } catch (_: Exception) {
                        Log.e(TAG, "Не удалось прочитать тело ответа 404")
                    }
                    return Result.failure(Exception("Пользователь не найден на сервере. Зарегистрируйтесь заново."))
                }
                val userRoleResponse = response.body<UserRoleResponse>()
                val role = UserRole.valueOf(userRoleResponse.role.uppercase())
                cachedRole = role
                Log.d(TAG, "Роль получена с сервера: $role")
                Result.success(role)
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка получения роли: ${e.message}", e)
                try {
                    val errorBody = ktorClient.get("/api/auth/role/$userId") {
                        header("Authorization", "Bearer $token")
                    }.bodyAsText()
                    Log.e(TAG, "Ответ сервера: $errorBody")
                } catch (e2: Exception) {
                    Log.e(TAG, "Не удалось получить тело ответа: ${e2.message}")
                }
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String, role: UserRole): Result<Unit> {
        return try {
            Log.d(TAG, "Попытка регистрации: $email, роль: $role")
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Log.d(TAG, "Пользователь создан в Firebase")

            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Log.d(TAG, "Пользователь авторизован после регистрации")

            val token = getAuthToken()
            val userId = firebaseAuth.currentUser?.uid
            val userEmail = firebaseAuth.currentUser?.email

            Log.d(TAG, "Token получен: ${token?.take(20)}...")
            Log.d(TAG, "userId: $userId")
            Log.d(TAG, "userEmail: $userEmail")

            if (token.isNullOrEmpty() || userId.isNullOrEmpty() || userEmail.isNullOrEmpty()) {
                Log.e(TAG, "Недостаточно данных для регистрации на сервере")
                return Result.failure(Exception("Не удалось получить данные пользователя после регистрации"))
            }

            try {
                Log.d(TAG, "Отправляю RegisterRequest: id=$userId, email=$userEmail, role=${role.name.lowercase()}")

                val response = ktorClient.post("/api/auth/register") {
                    header("Authorization", "Bearer $token")
                    setBody(RegisterRequest(id = userId, email = userEmail, role = role.name.lowercase()))
                }

                Log.d(TAG, "Ответ сервера: ${response.status.value}")

                cachedRole = role
                Log.d(TAG, "Регистрация успешна, роль кэширована: $role")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при отправке RegisterRequest: ${e.message}", e)
                Result.failure(e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка регистрации: ${e.message}", e)
            Result.failure(e)
        }
    }


    override fun getCurrentUserRole(): UserRole? {
        if (firebaseAuth.currentUser == null) return null
        return cachedRole
    }

    override fun logout() {
        firebaseAuth.signOut()
        cachedRole = null
    }
}
