package com.example.slimyan.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.slimyan.ui.home.HomeScreen
import com.example.slimyan.ui.meal.MealScreen
import com.example.slimyan.ui.settings.SettingsScreen
import com.example.slimyan.ui.weight.WeightScreen
import com.example.slimyan.ui.workout.WorkoutScreen

private enum class Dest(val route: String, val label: String, val icon: ImageVector) {
    Home("home", "ホーム", Icons.Filled.Home),
    Meal("meal", "食事", Icons.Filled.Restaurant),
    Workout("workout", "筋トレ", Icons.Filled.FitnessCenter),
    Weight("weight", "体重", Icons.Filled.MonitorWeight),
    Settings("settings", "設定", Icons.Filled.Settings),
}

@Composable
fun SlimYanApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = backStackEntry?.destination

    fun navigateTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                Dest.entries.forEach { dest ->
                    val selected = currentDest?.hierarchy?.any { it.route == dest.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = { navigateTab(dest.route) },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Dest.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Dest.Home.route) {
                HomeScreen(onNavigateToSettings = { navigateTab(Dest.Settings.route) })
            }
            composable(Dest.Meal.route) { MealScreen() }
            composable(Dest.Workout.route) { WorkoutScreen() }
            composable(Dest.Weight.route) { WeightScreen() }
            composable(Dest.Settings.route) { SettingsScreen() }
        }
    }
}
