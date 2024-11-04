package com.example.weatherforecastapplication.alarm.broadcast

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.weatherforecastapplication.alarm.viewmodel.AlarmViewModel
import com.example.weatherforecastapplication.alarm.viewmodel.AlarmViewModelFactory

class DismissAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Cancel the alarm when dismiss action is triggered
        val alarmName = intent.getStringExtra("alarmName") ?: return
        val requestCode = alarmName.hashCode() // Assuming you used the alarm name to generate a unique request code
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarmName", alarmName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent) // Cancel the alarm
        Toast.makeText(context, "Alarm dismissed", Toast.LENGTH_SHORT).show()
    }
}
