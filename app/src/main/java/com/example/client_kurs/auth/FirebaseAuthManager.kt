package com.example.client_kurs.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import java.io.IOException

/**
 * Обёртка над Firebase Authentication.
 * Предоставляет suspend-методы для входа, регистрации и работы с токеном.
 * Firestore **не используется** — только email/password аутентификация.
 */
class FirebaseAuthManager(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    /**
     * Выполняет вход по email и паролю.
     * @return [FirebaseUser] при успехе.
     * @throws IOException при сетевой ошибке или неверных учётных данных.
     */
    suspend fun signIn(email: String, password: String): FirebaseUser {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user ?: throw IOException("Пользователь не найден после входа")
        } catch (e: Exception) {
            throw IOException("Ошибка входа: ${e.message}", e)
        }
    }

    /**
     * Регистрирует нового пользователя по email и паролю.
     * @return [FirebaseUser] созданного пользователя.
     * @throws IOException при ошибке регистрации.
     */
    suspend fun signUp(email: String, password: String): FirebaseUser {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user ?: throw IOException("Пользователь не создан")
        } catch (e: Exception) {
            throw IOException("Ошибка регистрации: ${e.message}", e)
        }
    }

    /**
     * Возвращает текущего авторизованного пользователя или `null`, если пользователь не вошёл.
     */
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    /**
     * Получает ID-токен текущего пользователя (JWT) из Firebase.
     * @param forceRefresh если `true` — принудительно обновляет токен.
     * @return строка токена или `null`, если пользователь не авторизован.
     * @throws IOException при сетевой ошибке получения токена.
     */
    suspend fun getIdToken(forceRefresh: Boolean = false): String? {
        return try {
            auth.currentUser?.getIdToken(forceRefresh)?.await()?.token
        } catch (e: Exception) {
            throw IOException("Ошибка получения токена: ${e.message}", e)
        }
    }

    /** Выполняет выход из аккаунта. */
    fun signOut() {
        auth.signOut()
    }
}