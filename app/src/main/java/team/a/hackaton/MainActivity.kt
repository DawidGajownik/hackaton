package team.a.hackaton

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telecom.TelecomManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import team.a.hackaton.navigation.AppNavigation
import team.a.hackaton.screens.ThreeTilesScreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val openComposable = intent.getBooleanExtra("openComposable", false)
        fun scheduleDailyAlarm(context: Context, hour: Int = 18, minute: Int = 43) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }
            if (calendar.timeInMillis < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
        scheduleDailyAlarm(this)
        val fromDailyAlarm = intent.getBooleanExtra("openComposable", false)
        val fromPushNotification = intent.getStringExtra("destination") == "admin"

        val startDestination = when {
            fromPushNotification -> "admin"  // From Firebase notification
            fromDailyAlarm -> "special"     // From daily alarm notification
            else -> "dialer"                // Normal app start
        }
        setContent {
            if (openComposable) {
                // Tutaj wstaw swój Composable, który ma się otworzyć po kliknięciu powiadomienia
                MySpecialComposable2()
            } else {
                FakeHomeScreen(onLogEvent = { eventType, metadata -> logUserEvent(eventType, metadata) })
            }
        }

        // ustaw alarm przy starcie aplikacji
    }
    override fun onResume() {
        super.onResume()
        // When the app comes to the foreground, log the activity.
        logUserEvent("APP_RESUMED")
    }

    // --- ADD THIS HELPER FUNCTION ---
    private fun logUserEvent(eventType: String, metadata: Map<String, String>? = null) {
        // Use a unique, easy-to-find log tag
        val logTag = "UserEventLogger"

        Log.d(logTag, "Attempting to fetch FCM token for event: $eventType")

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e(logTag, ">>>> FETCHING FCM TOKEN FAILED", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            Log.d(logTag, ">>>> SUCCESSFULLY FETCHED TOKEN: $token")

            // Now, send it to our logger
            ActivityLogger.logEvent(token, eventType, metadata)
        }
    }
}
@Composable
fun MySpecialComposable() {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF6200EE)) // fioletowe tło
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Panel akcji",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Siatka 3x3
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                for (row in 0 until 3) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        for (col in 0 until 3) {
                            val index = row * 3 + col + 1
                            IconButton(
                                onClick = {
                                    Toast.makeText(context, "Kliknięto ikonę $index", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(Color.White.copy(alpha = 0.2f), shape = CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Ikona $index",
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun FakeHomeScreen(onLogEvent: (eventType: String, metadata: Map<String, String>?) -> Unit) {
    val context = LocalContext.current
    var showLoading by remember { mutableStateOf(false) }
    var openPlayCare by remember { mutableStateOf(false) }

    when {
        showLoading -> {
            PlayCareLoadingScreen(
                onFinished = {
                    showLoading = false
                    openPlayCare = true
                }
            )
            return
        }
        openPlayCare -> {
            ThreeTilesScreen(
                onDailyAlarmClick = { /* akcja */ },
                onBackToHome = {
                    openPlayCare = false
                }
            )
            return
        }
    }

    val time by remember {
        mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
    }
    val date by remember {
        mutableStateOf(SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date()))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF512DA8), Color(0xFF673AB7), Color(0xFF9575CD))
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Góra – godzina i data
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = time,
                    style = MaterialTheme.typography.displayLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = date.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.White.copy(alpha = 0.8f))
                )
            }

            // Środek – ikony aplikacji (3x2)
            val apps = listOf(
                "📞" to "Telefon",
                "💬" to "Wiadomości",
                "📷" to "Aparat",
                "⚙️" to "Ustawienia",
                "CARE" to "Play Care",
                "🎵" to "Muzyka"
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                for (row in 0 until 2) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(36.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (col in 0 until 3) {
                            val index = row * 3 + col
                            val (emojiOrText, label) = apps[index]

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    when (label) {
                                        "Play Care" -> showLoading = true
                                        else -> Toast.makeText(
                                            context,
                                            "Otwieram $label",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(
                                            if (label == "Play Care")
                                                Color(0xFF7E57C2)
                                            else
                                                Color.White.copy(alpha = 0.2f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (label == "Play Care") {
                                        Text(
                                            text = "CARE",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    } else {
                                        Text(
                                            text = emojiOrText,
                                            fontSize = 32.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = label, color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
            val buttonColors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF6200EE)
            )
            Button(
                onClick = { // Log the event with specific metadata for this button
                    onLogEvent("BUTTON_CLICK", mapOf("buttonId" to "call_piotr"))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = buttonColors
            ) {
                Text("Piotr")
            }
            // Dół – pasek gestów
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(6.dp)
                        .background(Color.White.copy(alpha = 0.6f), shape = RoundedCornerShape(50))
                )
            }
        }
    }

}

@Composable
fun PlayCareLoadingScreen(onFinished: () -> Unit) {
    var scale by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        ) { value, _ ->
            scale = value
        }
        delay(2000)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF7E57C2)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "CARE",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (80 * scale).sp
        )
    }

}



@Composable
fun SafeAppNavigation() {
    // Po prostu wywołujemy AppNavigation() w runtime
    AppNavigation()
}

@Composable
fun MySpecialComposable2() {
    val context = LocalContext.current
    val emojis = listOf("😀", "🚀", "🎵", "🍕")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF6200EE))
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Panel akcji",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                for (row in 0 until 2) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        for (col in 0 until 2) {
                            val index = row * 2 + col
                            IconButton(
                                onClick = {
                                    Toast.makeText(context, "Kliknięto: ${emojis[index]}", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(Color.White.copy(alpha = 0.2f), shape = CircleShape)
                            ) {
                                Text(
                                    text = emojis[index],
                                    fontSize = 36.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
// --- MODIFIED: Added onLogEvent function parameter ---
fun SimpleDialerUI(
    navController: NavController,
    onLogEvent: (eventType: String, metadata: Map<String, String>?) -> Unit
) {
    val context = LocalContext.current
    var number by remember { mutableStateOf("") }

    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) Toast.makeText(context, "Uprawnienie przyznane", Toast.LENGTH_SHORT).show()
            else Toast.makeText(context, "Brak uprawnień", Toast.LENGTH_SHORT).show()
        }
    )

    fun startCallTo(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            context.startActivity(intent)
        } else {
            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }

    // 🌸 Fioletowe tło
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF6200EE))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = number,
                onValueChange = { number = it },
                label = { Text("Numer telefonu", color = Color.White) },
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                    cursorColor = Color.White
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            val buttonColors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF6200EE)
            )

            Button(
                onClick = {
                    if (number.isNotEmpty()) startCallTo(number)
                    else Toast.makeText(context, "Wpisz numer", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = buttonColors
            ) {
                Text("Zadzwoń")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { startCallTo("500677381") }, modifier = Modifier.fillMaxWidth(), colors = buttonColors) {
                Text("Piotr")
            }
            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = { startCallTo("732242825") }, modifier = Modifier.fillMaxWidth(), colors = buttonColors) {
                Text("Rafał")
            }
            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = { startCallTo("531401474") }, modifier = Modifier.fillMaxWidth(), colors = buttonColors) {
                Text("Dawid")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                val telecomManager = context.getSystemService(TelecomManager::class.java)
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED) {
                    telecomManager.acceptRingingCall()
                } else {
                    callPermissionLauncher.launch(Manifest.permission.ANSWER_PHONE_CALLS)
                }
            }, modifier = Modifier.fillMaxWidth(), colors = buttonColors) {
                Text("Odbierz połączenie")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                try {
                    val telecomManager = context.getSystemService(TelecomManager::class.java)
                    telecomManager.endCall()
                } catch (e: Exception) {
                    Toast.makeText(context, "Nie można zakończyć połączenia", Toast.LENGTH_SHORT).show()
                }
            }, modifier = Modifier.fillMaxWidth(), colors = buttonColors) {
                Text("Zakończ połączenie")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

