package team.a.hackaton.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import team.a.hackaton.screens.DailyAlarmScreen
import team.a.hackaton.screens.ThreeTilesScreen

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf("three_tiles") }

    when (currentScreen) {
       /* "three_tiles" -> ThreeTilesScreen(
            onDailyAlarmClick = {
                currentScreen = "daily_alarm"
            },
            onBackToHome = TODO()
        )*/
        "daily_alarm" -> DailyAlarmScreen(
            onBackClick = {
                currentScreen = "three_tiles"
            }
        )
    }
}