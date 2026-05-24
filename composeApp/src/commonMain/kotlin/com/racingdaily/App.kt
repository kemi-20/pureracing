package com.racingdaily

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.savedstate.read
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
import com.racingdaily.ui.theme.LocalHazeState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

@Composable
fun App() {
    val hazeState = remember { HazeState() }

    RacingDailyTheme {
        CompositionLocalProvider(LocalHazeState provides hazeState) {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            val showBottomBar = currentDestination?.route in listOf(
                Routes.Home, Routes.Race, Routes.Rankings, Routes.More
            )

            Scaffold(
                modifier = Modifier.fillMaxSize(),
            ) { innerPadding ->
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = innerPadding.calculateTopPadding()) // 仅 padding top，底栏不占位
                            .haze(hazeState)
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = Routes.Home
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
                                val articleId = backStackEntry.arguments?.read { getString("articleId") }?.toIntOrNull() ?: 0
                                NewsDetailScreen(navController = navController, articleId = articleId)
                            }
                            composable("${Routes.SessionDetail}/{gpId}/{sessionId}") { backStackEntry ->
                                val gpId = backStackEntry.arguments?.read { getString("gpId") }?.toIntOrNull() ?: 0
                                val sessionId = backStackEntry.arguments?.read { getString("sessionId") }?.toIntOrNull() ?: 0
                                SessionDetailScreen(navController = navController, gpId = gpId, sessionId = sessionId)
                            }
                            composable("${Routes.TrackDetail}/{trackId}") { backStackEntry ->
                                val trackId = backStackEntry.arguments?.read { getString("trackId") }?.toIntOrNull() ?: 0
                                TrackScreen(navController = navController, trackId = trackId)
                            }
                            composable("${Routes.ChampionshipDriver}/{category}/{id}") { backStackEntry ->
                                val category = backStackEntry.arguments?.read { getString("category") } ?: "custom"
                                val id = backStackEntry.arguments?.read { getString("id") }?.toIntOrNull() ?: 0
                                RankingScreen(navController = navController, category = category, championshipId = id)
                            }
                        }
                    }

                    if (showBottomBar) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = innerPadding.calculateBottomPadding())
                        ) {
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
                }
            }
        }
    }
}
