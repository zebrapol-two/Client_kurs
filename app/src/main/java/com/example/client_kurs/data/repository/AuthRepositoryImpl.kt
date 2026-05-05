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
import io.ktor.client.request.setBody
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    // We cache the current role in memory for quick access (or we could fetch it every time)
    private var cachedRole: UserRole? = null

    override suspend fun login(email: String, password: String): Result<UserRole> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val token = getAuthToken()
            val response = ktorClient.get("/api/auth/role") {
                header("Authorization", "Bearer $token")
            }.body<UserRoleResponse>()

            val role = UserRole.valueOf(response.role.uppercase())
            cachedRole = role
            Result.success(role)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String, role: UserRole): Result<Unit> {
        return try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val token = getAuthToken()

            ktorClient.post("/api/auth/register") {
                header("Authorization", "Bearer $token")
                setBody(RegisterRequest(role.name.uppercase()))
            }
            cachedRole = role
            Result.success(Unit)
        } catch (e: Exception) {
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

