package com.example.client_kurs.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.client_kurs.domain.model.UserRole

@Composable
fun StockKeeperDrawer(
    role: UserRole?,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allItems = drawerItemsForRole(role)
    val mainItems = allItems.filterNot { it.isBottomAction }
    val bottomItems = allItems.filter { it.isBottomAction }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DrawerHeader(role = role)

        HorizontalDivider()

        mainItems.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                icon = { Icon(item.icon, contentDescription = item.title) },
                onClick = { onNavigate(item.route) },
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = Color.Transparent
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider()

        bottomItems.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.title) },
                selected = false,
                icon = { Icon(item.icon, contentDescription = item.title) },
                onClick = {
                    if (item == DrawerMenuItem.Login) {
                        onLogout()
                    } else {
                        onNavigate(item.route)
                    }
                },
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = Color.Transparent
                )
            )
        }

        NavigationDrawerItem(
            label = { Text("Выйти") },
            selected = false,
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Выйти"
                )
            },
            onClick = onLogout,
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun DrawerHeader(role: UserRole?) {
    val roleText = when (role) {
        UserRole.CUSTOMER -> "Покупатель"
        UserRole.STOREKEEPER -> "Кладовщик"
        null -> "Гость"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "StockKeeper",
                style = MaterialTheme.typography.titleMedium
            )
        }
        Text(
            text = "Роль: $roleText",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
    }
}