package com.example.client_kurs.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.client_kurs.presentation.viewmodel.AnalyticsViewModel
import com.example.client_kurs.presentation.viewmodel.InventoryViewModel
import com.example.client_kurs.presentation.viewmodel.PendingDeliveriesViewModel
import com.example.client_kurs.presentation.viewmodel.RecommendOrderViewModel
import com.example.client_kurs.presentation.viewmodel.StorekeeperViewModel
import com.example.client_kurs.presentation.viewmodel.SupplierComparisonViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun StorekeeperNavGraph(
    onLogout: () -> Unit,
    navController: NavHostController       // контроллер передаётся извне
) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.STOREKEEPER_HOME
    ) {
        composable(NavigationRoutes.STOREKEEPER_HOME) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(NavigationRoutes.STOREKEEPER_HOME)
            }
            val viewModel = koinViewModel<StorekeeperViewModel>(viewModelStoreOwner = parentEntry)
            StorekeeperHomeScreen(
                viewModel = viewModel,
                currentRoute = NavigationRoutes.STOREKEEPER_HOME,
                onNavigate = { route ->
                    if (route != NavigationRoutes.STOREKEEPER_HOME) {
                        navController.navigate(route) { launchSingleTop = true }
                    }
                },
                onLogout = onLogout
            )
        }

        composable(NavigationRoutes.STOREKEEPER_ADD) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(NavigationRoutes.STOREKEEPER_HOME)
            }
            val viewModel = koinViewModel<StorekeeperViewModel>(viewModelStoreOwner = parentEntry)
            AddProductScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavigationRoutes.STOREKEEPER_LOW_INVENTORY) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(NavigationRoutes.STOREKEEPER_HOME)
            }
            val viewModel = koinViewModel<StorekeeperViewModel>(viewModelStoreOwner = parentEntry)
            LowInventoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavigationRoutes.STOREKEEPER_INVENTORY) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(NavigationRoutes.STOREKEEPER_HOME)
            }
            val viewModel = koinViewModel<InventoryViewModel>(viewModelStoreOwner = parentEntry)
            InventoryScreen(
                viewModel = viewModel,
                currentRoute = NavigationRoutes.STOREKEEPER_INVENTORY,
                onNavigate = { route -> navController.navigate(route) { launchSingleTop = true } },
                onLogout = onLogout
            )
        }

        composable(NavigationRoutes.STOREKEEPER_SUPPLIER_COMPARISON) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(NavigationRoutes.STOREKEEPER_HOME)
            }
            val viewModel = koinViewModel<SupplierComparisonViewModel>(viewModelStoreOwner = parentEntry)
            SupplierComparisonScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavigationRoutes.STOREKEEPER_ANALYTICS) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(NavigationRoutes.STOREKEEPER_HOME)
            }
            val viewModel = koinViewModel<AnalyticsViewModel>(viewModelStoreOwner = parentEntry)
            AnalyticsScreen(
                viewModel = viewModel,
                currentRoute = NavigationRoutes.STOREKEEPER_ANALYTICS,
                onNavigate = { route -> navController.navigate(route) { launchSingleTop = true } },
                onLogout = onLogout
            )
        }

        composable(NavigationRoutes.STOREKEEPER_RECOMMEND_ORDER) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(NavigationRoutes.STOREKEEPER_HOME)
            }
            val viewModel = koinViewModel<RecommendOrderViewModel>(viewModelStoreOwner = parentEntry)
            RecommendOrderScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavigationRoutes.STOREKEEPER_PENDING_DELIVERIES) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(NavigationRoutes.STOREKEEPER_HOME)
            }
            val pendingViewModel = koinViewModel<PendingDeliveriesViewModel>(viewModelStoreOwner = parentEntry)
            val storekeeperViewModel = koinViewModel<StorekeeperViewModel>(viewModelStoreOwner = parentEntry)

            PendingDeliveriesScreen(
                viewModel = pendingViewModel,
                onBack = { navController.popBackStack() },
                onDeliveryReceived = {
                    storekeeperViewModel.loadProducts()
                }
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