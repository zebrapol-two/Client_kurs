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

<<<<<<< HEAD
            // Пытаемся получить роль с сервера
            try {
                Log.d(TAG, "Запрос на /api/auth/role/$userId")
                val response = ktorClient.get("/api/auth/role/$userId") {
                    header("Authorization", "Bearer $token")
=======
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
<<<<<<< HEAD
        // Роль уже установлена на сервере при регистрации, этот метод не нужен.
        // Если нужно менять роль – нужен отдельный эндпоинт.
        userPreferencesManager.saveRole(role)
        return Result.success(Unit)
=======
        return try {
            val token = getAuthToken()
            val userId = firebaseAuth.currentUser?.uid
            val userEmail = firebaseAuth.currentUser?.email

            if (token.isNullOrEmpty() || userId.isNullOrEmpty() || userEmail.isNullOrEmpty()) {
                return Result.failure(Exception("Не удалось получить данные пользователя"))
            }

            Log.d(TAG, "Отправка RegisterRequest: id=$userId, role=${role.name.lowercase()}")
            ktorClient.post("/auth/register") {
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
>>>>>>> 6d7f8b3 (Исправление логики отправки запросов на получение имеющихся товаров на складе)
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
>>>>>>> 8119b08 (Исправление логики отправки запросов на получение имеющихся товаров на складе)
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
