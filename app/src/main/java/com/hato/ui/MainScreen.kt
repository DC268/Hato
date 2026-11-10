package com.hato.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hato.ui.map.CreateZoneState
import com.hato.ui.map.MapScreen
import com.hato.ui.map.MapViewModel
import com.hato.ui.screens.AlertsScreen
import com.hato.ui.screens.AnimalsScreen
import com.hato.ui.screens.SettingsScreen
import com.hato.ui.screens.ZonesScreen
import com.hato.ui.theme.*

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Map : Screen("map", "Mapa", Icons.Default.Place)
    object Zones : Screen("zones", "Potreros", Icons.Default.Home)
    object Alerts : Screen("alerts", "Alertas", Icons.Default.Notifications)
    object Animals : Screen("animals", "Hato", Icons.AutoMirrored.Filled.List)
    object Settings : Screen("settings", "Ajustes", Icons.Default.Settings)
}

@Composable
fun MainScreen(
    viewModel: MapViewModel,
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()
    val items = listOf(Screen.Map, Screen.Zones, Screen.Alerts, Screen.Animals, Screen.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                contentColor = OnSurface,
                tonalElevation = 8.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title, style = MaterialTheme.typography.labelSmall) },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Primary,
                            selectedTextColor = Primary,
                            indicatorColor = SurfaceContainerHigh,
                            unselectedIconColor = OnSurfaceVariant,
                            unselectedTextColor = OnSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Map.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Map.route) { 
                MapScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToProfile = { navController.navigate("profile") }
                ) 
            }
            composable(Screen.Zones.route) { 
                ZonesScreen(
                    viewModel = viewModel,
                    onNavigateToMap = { 
                        viewModel.createZoneState = CreateZoneState.OPTIONS
                        navController.navigate(Screen.Map.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                ) 
            }
            composable(Screen.Alerts.route) { AlertsScreen(viewModel) }
            composable(Screen.Animals.route) { 
                AnimalsScreen(
                    viewModel = viewModel,
                    onNavigateToMap = {
                        navController.navigate(Screen.Map.route) {
                            popUpTo(Screen.Map.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                ) 
            }
            composable(Screen.Settings.route) { 
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { 
                        navController.navigate(Screen.Map.route) {
                            popUpTo(Screen.Map.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onLogout = onLogout
                ) 
            }
        }
    }
}
