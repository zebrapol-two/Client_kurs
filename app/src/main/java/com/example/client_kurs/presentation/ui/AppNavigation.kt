package com.example.client_kurs.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.client_kurs.presentation.viewmodel.AuthViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = koinViewModel()
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState.isInitialized, authState.isLoggedIn, authState.role) {
        if (!authState.isInitialized) return@LaunchedEffect
        val destination = when {
            !authState.isLoggedIn -> "login"
            authState.role == null -> "role_selection"
            else -> "role_aware"
        }
        navController.navigate(destination) {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {}
        composable("login") {
            LoginScreen(viewModel = authViewModel)
        }
        composable("role_selection") {
            RoleSelectionScreen(
                viewModel = authViewModel,
                onBackToAuth = { authViewModel.cancelRoleSelection() }
            )
        }
        composable("role_aware") {
            authState.role?.let { role ->
                RoleAwareNavHost(
                    role = role,
                    onLogout = { authViewModel.logout() }
                )
            }
        }
    }
}