package com.salvemidia.app.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.salvemidia.app.ui.theme.Background
import com.salvemidia.app.ui.theme.PurpleMain
import com.salvemidia.app.ui.theme.TextSecondary
import com.salvemidia.app.ui.theme.TextWhite

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        Screen.Home,
        Screen.History,
        Screen.Downloads
    )
    NavigationBar(
        containerColor = Background,
        contentColor = TextWhite
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
                    selectedIconColor = TextWhite,
                    unselectedIconColor = TextSecondary,
                    selectedTextColor = PurpleMain,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = PurpleMain
                )
            )
        }
    }
}
