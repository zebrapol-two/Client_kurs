package com.example.client_kurs.presentation.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.client_kurs.domain.model.UserRole

sealed class DrawerMenuItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val isBottomAction: Boolean = false
) {
    data object Catalog : DrawerMenuItem(
        route = NavigationRoutes.CUSTOMER_PRODUCTS,
        title = "Каталог",
        icon = Icons.Default.Home
    )

    data object Cart : DrawerMenuItem(
        route = NavigationRoutes.CUSTOMER_CART,
        title = "Корзина",
        icon = Icons.Default.ShoppingCart
    )

    data object OrderHistory : DrawerMenuItem(
        route = NavigationRoutes.CUSTOMER_HISTORY,
        title = "История заказов",
        icon = Icons.Default.History
    )

    data object Warehouse : DrawerMenuItem(
        route = NavigationRoutes.STOREKEEPER_HOME,
        title = "Склад",
        icon = Icons.Default.Home
    )

    data object AddProduct : DrawerMenuItem(
        route = NavigationRoutes.STOREKEEPER_ADD,
        title = "Добавить товар",
        icon = Icons.Default.Add
    )

    data object LowStock : DrawerMenuItem(
        route = NavigationRoutes.STOREKEEPER_LOW_INVENTORY,
        title = "Низкий остаток",
        icon = Icons.Default.Warning
    )

    data object Inventory : DrawerMenuItem(
        route = NavigationRoutes.STOREKEEPER_INVENTORY,
        title = "Инвентаризация",
        icon = Icons.Default.ShoppingCart
    )

    data object SupplierComparison : DrawerMenuItem(
        route = NavigationRoutes.STOREKEEPER_SUPPLIER_COMPARISON,
        title = "Сравнение поставщиков",
        icon = Icons.Default.ShoppingCart
    )

    data object Analytics : DrawerMenuItem(
        route = NavigationRoutes.STOREKEEPER_ANALYTICS,
        title = "Аналитика",
        icon = Icons.Default.History
    )
    data object PendingDeliveries : DrawerMenuItem(
        route = NavigationRoutes.STOREKEEPER_PENDING_DELIVERIES,
        title = "Приёмка поставок",
        icon = Icons.Default.LocalShipping   // или другой значок, например Icons.Default.Inventory
    )

    data object Settings : DrawerMenuItem(
        route = NavigationRoutes.SETTINGS,
        title = "Настройки",
        icon = Icons.Default.Settings,
        isBottomAction = true
    )

    data object About : DrawerMenuItem(
        route = NavigationRoutes.ABOUT,
        title = "О приложении",
        icon = Icons.Default.Info,
        isBottomAction = true
    )

    data object Login : DrawerMenuItem(
        route = NavigationRoutes.LOGIN,
        title = "Вход",
        icon = Icons.AutoMirrored.Filled.ExitToApp,
        isBottomAction = true
    )
}

fun drawerItemsForRole(role: UserRole?): List<DrawerMenuItem> {
    val commonBottom = listOf(DrawerMenuItem.Settings, DrawerMenuItem.About)
    return when (role) {
        UserRole.CUSTOMER -> listOf(
            DrawerMenuItem.Catalog,
            DrawerMenuItem.Cart,
            DrawerMenuItem.OrderHistory
        ) + commonBottom

        UserRole.STOREKEEPER -> listOf(
            DrawerMenuItem.Warehouse,
            DrawerMenuItem.AddProduct,
            DrawerMenuItem.LowStock,
            DrawerMenuItem.Inventory,
            DrawerMenuItem.SupplierComparison,
            DrawerMenuItem.Analytics,
            DrawerMenuItem.PendingDeliveries
        ) + commonBottom

        null -> listOf(DrawerMenuItem.Login)
    }
}