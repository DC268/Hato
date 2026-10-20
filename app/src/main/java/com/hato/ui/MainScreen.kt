package com.hato.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Map : Screen("map", "Mapa", Icons.Default.Place)
    object Zones : Screen("zones", "Potreros", Icons.Default.Home)
    object Alerts : Screen("alerts", "Alertas", Icons.Default.Notifications)
    object Animals : Screen("animals", "Hato", Icons.Default.List)
    object Settings : Screen("settings", "Ajustes", Icons.Default.Settings)
}

@Composable
fun MainScreen(viewModel: MapViewModel) {
    val navController = rememberNavController()
    val items = listOf(Screen.Map, Screen.Zones, Screen.Alerts, Screen.Animals, Screen.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
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
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
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
            composable(Screen.Map.route) { MapScreen(viewModel) }
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
            composable(Screen.Alerts.route) { AlertsScreen() }
            composable(Screen.Animals.route) { AnimalsScreen(viewModel) }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}