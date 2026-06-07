package com.example.client_kurs.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import com.example.client_kurs.domain.model.UserRole
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    role: UserRole?,
    title: String,
    currentRoute: String,
    snackbarHostState: SnackbarHostState,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    topBarSearch: (@Composable () -> Unit)? = null,
    topBarActions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    val drawerContent: @Composable () -> Unit = {
        ModalDrawerSheet(drawerContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer) {
            StockKeeperDrawer(
                role = role,
                currentRoute = currentRoute,
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    onNavigate(route)
                },
                onLogout = {
                    scope.launch { drawerState.close() }
                    onLogout()
                }
            )
        }
    }

    BoxWithConstraints {
        val isLargeScreen = maxWidth >= 840.dp

        val scaffoldContent: @Composable () -> Unit = {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            if (topBarSearch != null) {
                                topBarSearch()
                            } else {
                                Text(title)
                            }
                        },
                        navigationIcon = {
                            if (!isLargeScreen) {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Меню")
                                }
                            }
                        },
                        actions = {
                            topBarActions()
                            IconButton(onClick = onLogout) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Выйти"
                                )
                            }
                        },
                        scrollBehavior = scrollBehavior,
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                },
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                content = content
            )
        }

        if (isLargeScreen) {
            PermanentNavigationDrawer(
                drawerContent = {
                    ModalDrawerSheet {
                        StockKeeperDrawer(
                            role = role,
                            currentRoute = currentRoute,
                            onNavigate = onNavigate,
                            onLogout = onLogout
                        )
                    }
                }
            ) {
                scaffoldContent()
            }
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = drawerContent,
                gesturesEnabled = true
            ) {
                scaffoldContent()
            }
        }
    }
}