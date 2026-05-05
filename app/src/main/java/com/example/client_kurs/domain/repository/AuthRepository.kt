package com.example.client_kurs.domain.repository

import com.example.client_kurs.domain.model.UserRole

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<UserRole>
    suspend fun register(email: String, password: String, role: UserRole): Result<Unit>
    fun getCurrentUserRole(): UserRole?
    fun logout()
}

