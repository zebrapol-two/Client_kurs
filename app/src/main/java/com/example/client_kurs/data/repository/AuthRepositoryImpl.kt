package com.example.client_kurs.data.repository

import android.util.Log
import com.example.client_kurs.data.local.UserPreferencesManager
import com.example.client_kurs.data.remote.dto.RegisterRequest
import com.example.client_kurs.data.remote.dto.UserRoleResponse
import com.example.client_kurs.data.remote.getAuthToken
import com.example.client_kurs.data.remote.ktorClient
import com.example.client_kurs.domain.model.UserRole
import com.example.client_kurs.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val userPreferencesManager: UserPreferencesManager
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepository"
    }

    override suspend fun login(email: String, password: String): Result<UserRole> {
        return try {
            Log.d(TAG, "Попытка логина: $email")
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val token = getAuthToken()
            val userId = firebaseAuth.currentUser?.uid

            if (token.isNullOrEmpty()) {
                Log.e(TAG, "Токен Firebase пуст")
                return Result.failure(Exception("Не удалось получить токен Firebase"))
            }
            if (userId.isNullOrEmpty()) {
                Log.e(TAG, "UID пользователя пуст")
                return Result.failure(Exception("Не удалось получить UID пользователя"))
            }

            Log.d(TAG, "Запрос на /api/auth/role/$userId")
            val response = ktorClient.get("/api/auth/role/$userId") {
                header("Authorization", "Bearer $token")
            }
            Log.d(TAG, "Ответ сервера статус: ${response.status.value}")

            if (response.status.value == 404) {
                return Result.failure(Exception("Пользователь не найден на сервере. Зарегистрируйтесь заново."))
            }

            val userRoleResponse = response.body<UserRoleResponse>()
            val role = UserRole.valueOf(userRoleResponse.role.uppercase())
            userPreferencesManager.saveRole(role)
            Log.d(TAG, "Роль получена с сервера: $role")
            Result.success(role)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка логина: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String): Result<Unit> {
        return try {
            Log.d(TAG, "Регистрация Firebase: $email")
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Log.d(TAG, "Firebase-пользователь создан")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка регистрации: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun saveRole(role: UserRole): Result<Unit> {
        return try {
            val token = getAuthToken()
            val userId = firebaseAuth.currentUser?.uid
            val userEmail = firebaseAuth.currentUser?.email

            if (token.isNullOrEmpty() || userId.isNullOrEmpty() || userEmail.isNullOrEmpty()) {
                return Result.failure(Exception("Не удалось получить данные пользователя"))
            }

            Log.d(TAG, "Отправка RegisterRequest: id=$userId, role=${role.name.lowercase()}")
            ktorClient.post("/api/auth/register") {
                header("Authorization", "Bearer $token")
                setBody(RegisterRequest(firebaseUid = userId, email = userEmail, role = role.name.lowercase()))
            }

            userPreferencesManager.saveRole(role)
            Log.d(TAG, "Роль сохранена: $role")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка сохранения роли: ${e.message}", e)
            Result.failure(e)
        }
    }

    override fun getRole(): UserRole? = userPreferencesManager.getRole()

    override fun isUserLoggedIn(): Boolean = firebaseAuth.currentUser != null

    override fun logout() {
        firebaseAuth.signOut()
        userPreferencesManager.clearRole()
        Log.d(TAG, "Пользователь вышел, роль очищена")
    }
}
