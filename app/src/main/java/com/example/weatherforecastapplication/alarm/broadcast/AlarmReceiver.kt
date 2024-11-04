package com.example.weatherforecastapplication.alarm.broadcast


import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi



class AlarmReceiver : BroadcastReceiver() {


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        val alarmName = intent.getStringExtra("alarmName") ?: "Alarm"
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)


        val dismissIntent = Intent(context, DismissAlarmReceiver::class.java).apply {
            putExtra("alarmName", alarmName)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        startAlarmService(context, alarmName, latitude, longitude)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startAlarmService(context: Context, alarmName: String, latitude: Double, longitude: Double) {
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("alarmName", alarmName)
            putExtra("latitude", latitude)
            putExtra("longitude", longitude)
        }
        context.startService(serviceIntent)
    }


}
