package com.racingdaily

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.racingdaily.ui.components.GlassBottomBar
import com.racingdaily.ui.navigation.Routes
import com.racingdaily.ui.screens.home.HomeScreen
import com.racingdaily.ui.screens.more.MoreScreen
import com.racingdaily.ui.screens.news.NewsDetailScreen
import com.racingdaily.ui.screens.race.RaceScreen
import com.racingdaily.ui.screens.race.SessionDetailScreen
import com.racingdaily.ui.screens.rankings.RankingScreen
import com.racingdaily.ui.screens.track.TrackScreen
import com.racingdaily.ui.theme.RacingDailyTheme

@Composable
fun App() {
    RacingDailyTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        val showBottomBar = currentDestination?.route in listOf(
            Routes.Home, Routes.Race, Routes.Rankings, Routes.More
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (showBottomBar) {
                    GlassBottomBar(
                        currentRoute = currentDestination?.route,
                        onTabSelected = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Routes.Home,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Routes.Home) {
                    HomeScreen(navController = navController)
                }
                composable(Routes.Race) {
                    RaceScreen(navController = navController)
                }
                composable(Routes.Rankings) {
                    RankingScreen(navController = navController)
                }
                composable(Routes.More) {
                    MoreScreen(navController = navController)
                }
                composable("${Routes.NewsDetail}/{articleId}") { backStackEntry ->
                    val articleId = backStackEntry.arguments?.getString("articleId")?.toIntOrNull() ?: 0
                    NewsDetailScreen(navController = navController, articleId = articleId)
                }
                composable("${Routes.SessionDetail}/{gpId}/{sessionId}") { backStackEntry ->
                    val gpId = backStackEntry.arguments?.getString("gpId")?.toIntOrNull() ?: 0
                    val sessionId = backStackEntry.arguments?.getString("sessionId")?.toIntOrNull() ?: 0
                    SessionDetailScreen(navController = navController, gpId = gpId, sessionId = sessionId)
                }
                composable("${Routes.TrackDetail}/{trackId}") { backStackEntry ->
                    val trackId = backStackEntry.arguments?.getString("trackId")?.toIntOrNull() ?: 0
                    TrackScreen(navController = navController, trackId = trackId)
                }
                composable("${Routes.ChampionshipDriver}/{category}/{id}") { backStackEntry ->
                    val category = backStackEntry.arguments?.getString("category") ?: "custom"
                    val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 0
                    RankingScreen(navController = navController, category = category, championshipId = id)
                }
            }
        }
    }
}
