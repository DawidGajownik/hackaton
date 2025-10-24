package team.a.hackaton

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.util.Calendar

class DailyAlarmService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createNotification())

        // Ustaw alarm na następny dzień 13:00
        scheduleNextAlarm()

        // Uruchom ekran od razu
        val activityIntent = Intent(this, MainActivity::class.java)
        activityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activityIntent.putExtra("openComposable", true)
        startActivity(activityIntent)

        // Serwis może się sam zakończyć, jeśli nie potrzebujesz dalszego działania
        stopForeground(true)
        stopSelf()
        return START_NOT_STICKY
    }

    private fun createNotification(): Notification {
        val channelId = "foreground_service_channel"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Foreground Service",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Daily Alarm Service")
            .setContentText("Serwis działa w tle")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }

    private fun scheduleNextAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, DailyAlarmService::class.java)
        val pendingIntent = PendingIntent.getService(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 16)
            set(Calendar.MINUTE, 44)
            set(Calendar.SECOND, 0)
            if (timeInMillis < System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
