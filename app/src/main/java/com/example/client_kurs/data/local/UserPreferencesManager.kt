package com.example.client_kurs.data.local

import android.content.Context
import com.example.client_kurs.domain.model.UserRole

class UserPreferencesManager(context: Context) {

    private val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    // Роль
    fun saveRole(role: UserRole) {
        prefs.edit().putString(KEY_ROLE, role.name).commit()
    }

    fun getRole(): UserRole? {
        val roleName = prefs.getString(KEY_ROLE, null) ?: return null
        return try {
            UserRole.valueOf(roleName)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    fun clearRole() {
        prefs.edit().remove(KEY_ROLE).commit()
    }

    // Access token (JWT от сервера)
    fun saveAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).commit()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    // Refresh token
    fun saveRefreshToken(token: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, token).commit()
    }

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    // Полная очистка при выходе
    fun clearAll() {
        prefs.edit().clear().commit()
    }

    companion object {
        private const val KEY_ROLE = "user_role"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}