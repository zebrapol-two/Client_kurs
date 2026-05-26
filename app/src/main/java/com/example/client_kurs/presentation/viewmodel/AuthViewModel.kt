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
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            role = null
                        )
                    }
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

    fun saveRole(role: UserRole) {
        _authState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            authRepository.saveRole(role)
                .onSuccess {
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            role = role
                        )
                    }
                }
                .onFailure { error ->
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Ошибка сохранения роли"
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
        authRepository.logout()
        _authState.value = AuthState(
            isLoggedIn = false,
            role = null,
            isInitialized = true
        )
    }

    fun cancelRoleSelection() {
        logout()
    }
}
