package com.example.client_kurs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.client_kurs.domain.model.UserRole
import com.example.client_kurs.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val role: UserRole? = null,
    val isInitialized: Boolean = false
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState = _authState.asStateFlow()

    private var pendingEmail: String? = null
    private var pendingPassword: String? = null

    init {
        _authState.value = AuthState(
            isLoggedIn = authRepository.isUserLoggedIn(),
            role = authRepository.getRole(),
            isInitialized = true
        )
    }

    fun login(email: String, password: String) {
        _authState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            authRepository.login(email, password)
                .onSuccess { role ->
                    _authState.value = AuthState(
                        isLoading = false,
                        error = null,
                        isLoggedIn = true,
                        role = role,
                        isInitialized = true
                    )
                    pendingEmail = null
                    pendingPassword = null
                }
                .onFailure { error ->
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Ошибка входа"
                        )
                    }
                }
        }
    }

    fun register(email: String, password: String) {
        _authState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            authRepository.register(email, password)
                .onSuccess {
                    pendingEmail = email
                    pendingPassword = password
                    _authState.value = AuthState(
                        isLoading = false,
                        error = null,
                        isLoggedIn = true,
                        role = null,
                        isInitialized = true
                    )
                }
                .onFailure { error ->
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Ошибка регистрации"
                        )
                    }
                }
        }
    }

    fun submitRole(role: UserRole) {
        val email = pendingEmail
        val password = pendingPassword
        if (email == null || password == null) {
            _authState.update { it.copy(error = "Сначала зарегистрируйтесь") }
            return
        }
        _authState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val registerWithRoleResult = authRepository.registerWithRole(email, password, role.name.lowercase())
            if (registerWithRoleResult.isFailure) {
                _authState.update {
                    it.copy(
                        isLoading = false,
                        error = registerWithRoleResult.exceptionOrNull()?.message ?: "Ошибка сохранения роли"
                    )
                }
                return@launch
            }
            authRepository.login(email, password)
                .onSuccess { savedRole ->
                    _authState.value = AuthState(
                        isLoading = false,
                        error = null,
                        isLoggedIn = true,
                        role = savedRole,
                        isInitialized = true
                    )
                    pendingEmail = null
                    pendingPassword = null
                }
                .onFailure { error ->
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Ошибка входа после выбора роли"
                        )
                    }
                }
        }
    }

    fun getRole(): UserRole? = authRepository.getRole()

    fun clearError() {
        _authState.update { it.copy(error = null) }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState(
                isLoggedIn = false,
                role = null,
                isInitialized = true
            )
            pendingEmail = null
            pendingPassword = null
        }
    }

    fun cancelRoleSelection() {
        logout()
    }
}