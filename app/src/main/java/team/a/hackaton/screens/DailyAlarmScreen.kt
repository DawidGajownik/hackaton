package team.a.hackaton.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyAlarmScreen(
    onBackClick: () -> Unit
) {
    var alarmTime by remember { mutableStateOf(LocalTime.of(8, 0)) } // Default time: 8:00 AM
    var isAlarmEnabled by remember { mutableStateOf(true) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Format time for display (e.g., "08:00 AM")
    val formattedTime by remember(alarmTime) {
        derivedStateOf {
            val formatter = DateTimeFormatter.ofPattern("hh:mm a")
            alarmTime.format(formatter)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Daily Alarm Settings",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Alarm Time Picker Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Alarm Time",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Display current time
                    Text(
                        formattedTime,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Time Picker Button
                    Button(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("Choose Time")
                    }
                }
            }

            // Alarm Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Enable Alarm",
                        fontSize = 16.sp
                    )
                    Switch(
                        checked = isAlarmEnabled,
                        onCheckedChange = { isAlarmEnabled = it }
                    )
                }
            }

            // Days of Week Selection
            DaysOfWeekSelector()

            // Save Button
            Button(
                onClick = {
                    // Save alarm settings
                    // You can add your save logic here
                    onBackClick() // Go back after saving
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 16.dp)
            ) {
                Text("Save Alarm", fontSize = 16.sp)
            }
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            onTimeSelected = { hour, minute ->
                alarmTime = LocalTime.of(hour, minute)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Composable
fun DaysOfWeekSelector() {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    var selectedDays by remember { mutableStateOf(setOf<Int>()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Repeat",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // First row - shorter days
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                days.take(5).forEachIndexed { index, day ->
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        DayChip(
                            day = day,
                            isSelected = selectedDays.contains(index),
                            onClick = {
                                selectedDays = if (selectedDays.contains(index)) {
                                    selectedDays - index
                                } else {
                                    selectedDays + index
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Second row - longer days
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                days.takeLast(2).forEachIndexed { index, day ->
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        DayChip(
                            day = day,
                            isSelected = selectedDays.contains(index + 5),
                            onClick = {
                                val actualIndex = index + 5
                                selectedDays = if (selectedDays.contains(actualIndex)) {
                                    selectedDays - actualIndex
                                } else {
                                    selectedDays + actualIndex
                                }
                            }
                        )
                    }
                }
            }

            // Quick selection buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(
                    onClick = {
                        selectedDays = setOf(0, 1, 2, 3, 4)
                    }
                ) {
                    Text("Weekdays")
                }
                TextButton(
                    onClick = {
                        selectedDays = setOf(0, 1, 2, 3, 4, 5, 6)
                    }
                ) {
                    Text("Every Day")
                }
                TextButton(
                    onClick = {
                        selectedDays = emptySet()
                    }
                ) {
                    Text("Clear")
                }
            }
        }
    }
}

@Composable
fun DayChip(
    day: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    SuggestionChip(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        label = {
            Text(
                text = day,
                fontSize = 11.sp, // Smaller font for long names
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        colors = if (isSelected) {
            SuggestionChipDefaults.suggestionChipColors(
                containerColor = MaterialTheme.colorScheme.primary,
                labelColor = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            SuggestionChipDefaults.suggestionChipColors()
        }
    )
}

@Composable
fun TimePickerDialog(
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedHour by remember { mutableStateOf(8) }
    var selectedMinute by remember { mutableStateOf(0) }
    var isAm by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Time Picker
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    // Hour Picker
                    NumberPicker(
                        value = selectedHour,
                        onValueChange = { selectedHour = it },
                        range = 1..12,
                        modifier = Modifier.weight(1f)
                    )

                    Text(":", fontSize = 24.sp, modifier = Modifier.padding(horizontal = 8.dp))

                    // Minute Picker
                    NumberPicker(
                        value = selectedMinute,
                        onValueChange = { selectedMinute = it },
                        range = 0..59,
                        modifier = Modifier.weight(1f)
                    )

                    // AM/PM Picker
                    Column(modifier = Modifier.weight(1f)) {
                        TextButton(
                            onClick = { isAm = true },
                            colors = if (isAm) {
                                ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                ButtonDefaults.textButtonColors()
                            }
                        ) {
                            Text("AM", fontWeight = if (isAm) FontWeight.Bold else FontWeight.Normal)
                        }
                        TextButton(
                            onClick = { isAm = false },
                            colors = if (!isAm) {
                                ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                ButtonDefaults.textButtonColors()
                            }
                        ) {
                            Text("PM", fontWeight = if (!isAm) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Convert to 24-hour format
                    val hour24 = when {
                        isAm && selectedHour == 12 -> 0
                        isAm -> selectedHour
                        selectedHour == 12 -> 12
                        else -> selectedHour + 12
                    }
                    onTimeSelected(hour24, selectedMinute)
                }
            ) {
                Text("Set Time")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Increase button - Fixed: Use ArrowDropUp for increase
        IconButton(
            onClick = {
                val newValue = value + 1
                if (newValue <= range.last) {
                    onValueChange(newValue)
                } else {
                    onValueChange(range.first)
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp, // Fixed: Use ArrowDropUp for increase
                contentDescription = "Increase"
            )
        }

        // Display value
        Text(
            text = "%02d".format(value),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Decrease button
        IconButton(
            onClick = {
                val newValue = value - 1
                if (newValue >= range.first) {
                    onValueChange(newValue)
                } else {
                    onValueChange(range.last)
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Decrease"
            )
        }
    }
}