package com.salvemidia.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.salvemidia.app.ui.screens.downloads.DownloadsScreen
import com.salvemidia.app.ui.screens.history.HistoryScreen
import com.salvemidia.app.ui.screens.home.HomeScreen
import com.salvemidia.app.ui.screens.settings.SettingsScreen

@Composable
fun NavigationGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController, startDestination = Screen.Home.route, modifier = modifier) {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.History.route) {
            HistoryScreen()
        }
        composable(Screen.Downloads.route) {
            DownloadsScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
