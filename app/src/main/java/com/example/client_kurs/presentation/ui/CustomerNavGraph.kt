package com.example.client_kurs.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.client_kurs.presentation.viewmodel.CustomerViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun CustomerNavGraph(
    onLogout: () -> Unit,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.CUSTOMER_PRODUCTS
    ) {
        composable(NavigationRoutes.CUSTOMER_PRODUCTS) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(NavigationRoutes.CUSTOMER_PRODUCTS)
            }
            val customerViewModel = koinViewModel<CustomerViewModel>(
                viewModelStoreOwner = parentEntry
            )
            CustomerHomeScreen(
                viewModel = customerViewModel,
                onNavigateToCart = {
                    navController.navigate(NavigationRoutes.CUSTOMER_CART) {
                        launchSingleTop = true
                    }
                },
                onNavigateToHistory = {
                    navController.navigate(NavigationRoutes.CUSTOMER_HISTORY) {
                        launchSingleTop = true
                    }
                },
                onLogout = onLogout
            )
        }

        composable(NavigationRoutes.CUSTOMER_CART) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(NavigationRoutes.CUSTOMER_PRODUCTS)
            }
            val customerViewModel = koinViewModel<CustomerViewModel>(
                viewModelStoreOwner = parentEntry
            )
            CartScreen(
                viewModel = customerViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavigationRoutes.CUSTOMER_HISTORY) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(NavigationRoutes.CUSTOMER_PRODUCTS)
            }
            val customerViewModel = koinViewModel<CustomerViewModel>(
                viewModelStoreOwner = parentEntry
            )
            OrderHistoryScreen(
                viewModel = customerViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavigationRoutes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(NavigationRoutes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}