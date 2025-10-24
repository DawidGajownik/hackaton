package team.a.hackaton

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MyAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val activityIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("openComposable", true)
            }
            context.startActivity(activityIntent)
        }
    }
}
