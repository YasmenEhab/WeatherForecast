package com.example.weatherforecastapplication.alarm.broadcast

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.weatherforecastapplication.MainActivity
import com.example.weatherforecastapplication.R

class AlarmReceiver : BroadcastReceiver() {
    private var ringtone: Ringtone? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == "STOP_RINGTONE") {
            stopRingtone()
            return
        }

        val alarmId = intent.getIntExtra("alarmId", 1)
        val alarmName = intent.getStringExtra("alarmName") ?: "Alarm"
        Log.d("AlarmReceiver", "Alarm received: $alarmName")

        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)
        val hour = intent.getIntExtra("hour", 0)
        val minute = intent.getIntExtra("minute", 0)

        // Play sound
//        val alarmSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
//        ringtone = RingtoneManager.getRingtone(context, alarmSound)
//        ringtone?.play()

        // Show notification
        showNotification(context, alarmName)

        // Start AlarmService
        startAlarmService(context, alarmId, alarmName, latitude, longitude, hour, minute)
    }

    private fun showNotification(context: Context, alarmName: String) {
        Log.d("AlarmReceiver", "Showing notification for: $alarmName")

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "ALARM_CHANNEL_ID",
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for Alarm Notifications"
                enableLights(true)
                enableVibration(true)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), null)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "ALARM_CHANNEL_ID")
            .setSmallIcon(R.drawable.img_1)
            .setContentTitle("Alarm Notification")
            .setContentText("Alarm: $alarmName is going off!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startAlarmService(
        context: Context,
        alarmId: Int,
        alarmName: String,
        latitude: Double,
        longitude: Double,
        hour: Int,
        minute: Int
    ) {
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("alarmId", alarmId)
            putExtra("alarmName", alarmName)
            putExtra("latitude", latitude)
            putExtra("longitude", longitude)
            putExtra("hour", hour)
            putExtra("minute", minute)
        }
        context.startForegroundService(serviceIntent)
    }

    // Stop ringtone when notification is clicked
    private fun stopRingtone() {
        ringtone?.stop()
    }
}

