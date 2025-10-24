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
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val openComposable = intent.getBooleanExtra("openComposable", false)
        fun scheduleDailyAlarm(context: Context, hour: Int = 18, minute: Int = 8) {
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

        setContent {
            if (openComposable) {
                // Tutaj wstaw swój Composable, który ma się otworzyć po kliknięciu powiadomienia
                MySpecialComposable2()
            } else {
                SimpleDialerUI()
            }
        }

        // ustaw alarm przy starcie aplikacji
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
fun MySpecialComposable2() {
    val context = LocalContext.current

    val emojis = listOf("😀", "🚀", "🎵", "🍕") // 4 losowe emotikony

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
fun SimpleDialerUI() {
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

