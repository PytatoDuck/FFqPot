package com.pytato.ffqpot.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pytato.ffqpot.ui.components.TopBar
import com.pytato.ffqpot.ui.screens.HomeScreen
import com.pytato.ffqpot.ui.screens.RemuxerScreen
import com.pytato.ffqpot.ui.screens.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navItems = listOf(
        NavigationItem("Home", Icons.Default.Home, Destinations.Home.route),
        NavigationItem("Remuxer", Icons.Default.Build, Destinations.Remuxer.route),
        NavigationItem("Settings", Icons.Default.Settings, Destinations.Settings.route),
    )

    var currentRoute by remember { mutableStateOf(Destinations.Home.route) }
    var drawerOpener by remember { mutableStateOf<DrawerOpener?>(null) }

    AppNavigationDrawer(
        items = navItems,
        currentRoute = currentRoute,
        onItemClick = { item ->
            currentRoute = item.route
            navController.navigate(item.route) {
                popUpTo(navController.graph.startDestinationId) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    ) {
        drawerOpener = LocalDrawerOpener.current

        Scaffold(
            topBar = { TopBar { drawerOpener?.openDrawer() } }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Destinations.Home.route,
                modifier = Modifier.padding(padding)
            ) {
                composable(Destinations.Home.route) { HomeScreen() }
                composable(Destinations.Remuxer.route) { RemuxerScreen() }
                composable(Destinations.Settings.route) { SettingsScreen() }
            }
        }
    }
}
