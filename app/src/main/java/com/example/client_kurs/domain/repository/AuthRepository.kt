package com.example.client_kurs.domain.repository

import com.example.client_kurs.domain.model.UserRole

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<UserRole>
    /** Creates a Firebase account only. Role registration is done separately via [saveRole]. */
    suspend fun register(email: String, password: String): Result<Unit>
    suspend fun registerWithRole(email: String, password: String, role: String): Result<Unit>
    /** Sends role to the backend and persists it locally. */
    suspend fun saveRole(role: UserRole): Result<Unit>
    /** Returns the locally persisted role, or null if none saved. */
    fun getRole(): UserRole?
    /** True when a Firebase user is currently signed in. */
    fun isUserLoggedIn(): Boolean
    fun logout()
    suspend fun refreshAccessToken(): String?
}

