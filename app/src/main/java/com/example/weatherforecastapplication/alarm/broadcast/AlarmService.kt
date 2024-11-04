package com.example.weatherforecastapplication.alarm.broadcast

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.db.AppDatabase
import com.example.weatherforecastapplication.db.FavoriteCityLocalDataSourceImpl
import com.example.weatherforecastapplication.model.WeatherRepository
import com.example.weatherforecastapplication.model.WeatherRepositoryImpl
import com.example.weatherforecastapplication.network.RetrofitHelper
import com.example.weatherforecastapplication.network.WeatherRemoteDataSource
import com.example.weatherforecastapplication.network.WeatherRemoteDataSourceImpl
import com.example.weatherforecastapplication.network.WeatherService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class AlarmService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val CHANNEL_ID = "ALARM_CHANNEL_ID"
        private const val CHANNEL_NAME = "Weather Alert Channel"
        private const val CHANNEL_DESCRIPTION = "Channel for weather alerts"
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        loadUserPreferences()
        setLocale()

        // Initialize the repository for fetching weather data
        val favoriteCityDao = AppDatabase.getDatabase(this).favoriteCityDao()
        val favoriteCityLocalDataSource = FavoriteCityLocalDataSourceImpl(favoriteCityDao)
        val remoteDataSource = WeatherRemoteDataSourceImpl.getInstance(
            RetrofitHelper.getInstance().create(WeatherService::class.java)
        )
        weatherRepository = WeatherRepositoryImpl.getInstance(remoteDataSource, favoriteCityLocalDataSource)
    }

    private fun loadUserPreferences() {
        sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
    }

    private fun setLocale() {
        val languageOption = sharedPreferences.getString("LANGUAGE", "en") ?: "en"
        val locale = Locale(languageOption)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmName = intent?.getStringExtra("alarmName") ?: "Alarm"
        val latitude = intent?.getDoubleExtra("latitude", 0.0) ?: 0.0
        val longitude = intent?.getDoubleExtra("longitude", 0.0) ?: 0.0

        // Determine the type of alert based on user preference
        when (getAlertTypeFromSettings()) {
            "notification" -> showNotification(alarmName, latitude, longitude)
            "sound" -> playAlarmSound()
        }


        return START_NOT_STICKY
    }

    private fun playAlarmSound() {
        if (mediaPlayer == null) {
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            mediaPlayer = MediaPlayer.create(this, alarmSound).apply {
                isLooping = true
                start()
            }
        }
    }

    private fun getAlertTypeFromSettings(): String {
        return sharedPreferences.getString("ALARM_TYPE", "notification") ?: "notification"
    }

    private fun showNotification(alarmName: String, latitude: Double, longitude: Double) {
        // Create a notification channel if running on Android O and above
        createNotificationChannel()

        val dismissIntent = Intent(this, DismissAlarmReceiver::class.java).apply {
            putExtra("alarmName", alarmName) // Pass any necessary data
        }
        val dismissPendingIntent = PendingIntent.getService(this, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Create the dismiss action
        val dismissAction = NotificationCompat.Action(
            R.drawable.img_1,  // Replace with your dismiss icon
            "Dismiss",              // Text for the action
            dismissPendingIntent     // The pending intent to trigger on click
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(alarmName)

            .setSmallIcon(R.drawable.img_1)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(dismissAction)  // Add the action here
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }


    private fun createDismissAction(): PendingIntent {
        val dismissIntent = Intent(this, AlarmService::class.java).apply {
            action = "DISMISS_ALARM"
        }
        return PendingIntent.getService(this, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarmSound()
    }

    private fun stopAlarmSound() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        Toast.makeText(this, "Alarm Dismissed", Toast.LENGTH_SHORT).show()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
