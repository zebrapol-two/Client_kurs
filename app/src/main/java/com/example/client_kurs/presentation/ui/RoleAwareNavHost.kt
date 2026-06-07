package com.example.client_kurs.presentation.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.client_kurs.domain.model.UserRole

@Composable
fun RoleAwareNavHost(
    role: UserRole,
    onLogout: () -> Unit
) {
    when (role) {
        UserRole.CUSTOMER -> CustomerNavGraph(onLogout = onLogout)
        UserRole.STOREKEEPER -> {
            // Создаём контроллер один раз для всей сессии кладовщика
            val storekeeperNavController = rememberNavController()
            StorekeeperNavGraph(
                onLogout = onLogout,
                navController = storekeeperNavController
            )
        }
    }
}