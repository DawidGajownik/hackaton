package team.a.hackaton.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import team.a.hackaton.components.OptionTile

@Composable
fun ThreeTilesScreen(
    onDailyAlarmClick: () -> Unit,
    onBackToHome: () -> Unit // <── nowy callback do cofania
) {
    var selectedTile by remember { mutableStateOf<Int?>(null) }

    BackHandler(onBack = { onBackToHome() })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF512DA8), Color(0xFF673AB7), Color(0xFF9575CD))
                )
            )
            .padding(16.dp)
    ) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Play Care",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Surface(color = Color.White.copy(alpha = 0.15f), shape = MaterialTheme.shapes.medium) {
                    OptionTile(
                        title = "Senior",
                        icon = Icons.Default.Person,
                        options = listOf("Daily alarm", "Last activity", "Preferences"),
                        isExpanded = selectedTile == 1,
                        onClick = { selectedTile = if (selectedTile == 1) null else 1 },
                        onOptionClick = { option ->
                            if (option == "Daily alarm") onDailyAlarmClick()
                        }
                    )
                }

                Surface(color = Color.White.copy(alpha = 0.15f), shape = MaterialTheme.shapes.medium) {
                    OptionTile(
                        title = "Junior",
                        icon = Icons.Default.Person,
                        options = listOf("Daily alarm", "Last activity", "Preferences"),
                        isExpanded = selectedTile == 2,
                        onClick = { selectedTile = if (selectedTile == 2) null else 2 },
                        onOptionClick = { option ->
                            if (option == "Daily alarm") onDailyAlarmClick()
                        }
                    )
                }

                Surface(color = Color.White.copy(alpha = 0.15f), shape = MaterialTheme.shapes.medium) {
                    OptionTile(
                        title = "Main Settings",
                        icon = Icons.Default.Settings,
                        options = listOf("Accounts", "Caregivers", "Contact", "Help"),
                        isExpanded = selectedTile == 3,
                        onClick = { selectedTile = if (selectedTile == 3) null else 3 },
                        onOptionClick = { }
                    )
                }
            }
        }
    }
}
