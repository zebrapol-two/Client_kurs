package com.example.client_kurs.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.client_kurs.presentation.viewmodel.AuthViewModel
import com.example.client_kurs.presentation.viewmodel.CustomerViewModel
import com.example.client_kurs.presentation.viewmodel.StorekeeperViewModel
import org.koin.androidx.compose.koinViewModel
import androidx.navigation.navigation

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            val authViewModel = koinViewModel<AuthViewModel>()
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToCustomer = {
                    navController.navigate("customer_flow") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToStorekeeper = {
                    navController.navigate("storekeeper_home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        navigation(startDestination = "customer_main", route = "customer_flow") {
            composable("customer_main") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("customer_flow")
                }
                val customerViewModel = koinViewModel<CustomerViewModel>(viewModelStoreOwner = parentEntry)
                CustomerHomeScreen(
                    viewModel = customerViewModel,
                    onNavigateToCart = { navController.navigate("cart") }
                )
            }
            composable("cart") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("customer_flow")
                }
                val customerViewModel = koinViewModel<CustomerViewModel>(viewModelStoreOwner = parentEntry)
                CartScreen(
                    viewModel = customerViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable("storekeeper_home") {
            val storekeeperViewModel = koinViewModel<StorekeeperViewModel>()
            StorekeeperHomeScreen(viewModel = storekeeperViewModel)
        }
    }
}
