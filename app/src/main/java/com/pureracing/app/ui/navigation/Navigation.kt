package com.pureracing.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.pureracing.app.ui.screens.auth.LoginScreen
import com.pureracing.app.ui.screens.chat.ChatScreen
import com.pureracing.app.ui.screens.driver.DriverScreen
import com.pureracing.app.ui.screens.home.HomeScreen
import com.pureracing.app.ui.screens.race.RaceScreen
import com.pureracing.app.ui.screens.ranking.RankingScreen
import com.pureracing.app.ui.screens.track.TrackScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "首页", Icons.Default.Home)
    object Race : Screen("race", "赛事", Icons.Default.Flag)
    object Driver : Screen("driver", "车手", Icons.Default.Person)
    object Track : Screen("track", "赛道", Icons.Default.Map)
    object Ranking : Screen("ranking", "排名", Icons.Default.Leaderboard)
}

val bottomNavItems = listOf(Screen.Home, Screen.Race, Screen.Driver, Screen.Track, Screen.Ranking)

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "main") {
        composable("login") { LoginScreen(onLoginSuccess = { navController.navigate("main") { popUpTo("login") { inclusive = true } } }) }
        composable("main") { MainScreen() }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController, startDestination = Screen.Home.route) {
            composable(Screen.Home.route) { HomeScreen(padding) }
            composable(Screen.Race.route) { RaceScreen(padding) }
            composable(Screen.Driver.route) { DriverScreen(padding) }
            composable(Screen.Track.route) { TrackScreen(padding) }
            composable(Screen.Ranking.route) { RankingScreen(padding) }
        }
    }
}
