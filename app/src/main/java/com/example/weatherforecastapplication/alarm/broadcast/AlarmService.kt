package com.example.weatherforecastapplication.alarm.broadcast

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.weatherforecastapplication.MainActivity
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.alarm.view.Alarm
import com.example.weatherforecastapplication.db.AppDatabase
import com.example.weatherforecastapplication.db.LocalDataSourceImpl
import com.example.weatherforecastapplication.setting.SettingsManager
import com.example.weatherforecastapplication.model.WeatherRepository
import com.example.weatherforecastapplication.model.WeatherRepositoryImpl
import com.example.weatherforecastapplication.network.RetrofitHelper
import com.example.weatherforecastapplication.network.WeatherRemoteDataSourceImpl
import com.example.weatherforecastapplication.network.WeatherService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flowOn

import java.util.Locale

class AlarmService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var weatherRepository: WeatherRepository
    private val pendingIntentMap = mutableMapOf<Int, PendingIntent>()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()

        // Initialize the repository for fetching weather data
        val favoriteCityDao = AppDatabase.getDatabase(this).favoriteCityDao()
        val weatherDao = AppDatabase.getDatabase(this).weatherDao()
        val forecastDao = AppDatabase.getDatabase(this).forecastResponseDao()
        val favoriteCityLocalDataSource = LocalDataSourceImpl(favoriteCityDao, weatherDao, forecastDao)
        val remoteDataSource = WeatherRemoteDataSourceImpl.getInstance(
            RetrofitHelper.getInstance().create(WeatherService::class.java)
        )
        weatherRepository = WeatherRepositoryImpl.getInstance(remoteDataSource, favoriteCityLocalDataSource, this)

        // Initialize alarm sound
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        mediaPlayer = MediaPlayer.create(this, alarmSound)
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createNotification())

        // Extract intent data
        val alarmId = intent?.getIntExtra("alarmId", 1) ?: 1
        val alarmName = intent?.getStringExtra("alarmName") ?: "Alarm"
        val latitude = intent?.getDoubleExtra("latitude", 0.0) ?: 0.0
        val longitude = intent?.getDoubleExtra("longitude", 0.0) ?: 0.0
        val hour = intent?.getIntExtra("hour", 0) ?: 0
        val minute = intent?.getIntExtra("minute", 0) ?: 0
        val requestCode = alarmName.hashCode()

        val alarm = Alarm(
            id = alarmId,
            name = alarmName,
            time = "$hour:$minute",
            hour = hour,
            minute = minute,
            latitude = latitude,
            longitude = longitude
        )

        if (intent?.action == "DISMISS_ALARM") {
            dismissAlarm(requestCode)
            return START_NOT_STICKY
        }

        serviceScope.launch {
            weatherRepository.deleteAlarm(alarm) // Remove alarm from repository if needed
            handleAlarmSound() // Handle alarm sound based on user preference
            fetchWeather(latitude, longitude, alarmName) // Fetch weather and display notification
        }

        return START_NOT_STICKY
    }

    private fun dismissAlarm(requestCode: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = pendingIntentMap[requestCode] ?: PendingIntent.getBroadcast(
            this,
            requestCode,
            Intent(this, AlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntentMap.remove(requestCode)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        stopAlarmSound()
        notificationManager.cancelAll()
        stopForeground(true)
        stopSelf()
    }

    private fun fetchWeather(latitude: Double, longitude: Double, alarmName: String) {
        serviceScope.launch {
            try {
                val sharedPreferences = SettingsManager(applicationContext)
                val language = sharedPreferences.getLanguage() ?: "en"
                val cityName = getCityNameFromCoordinates(latitude, longitude)
                val apiKey = "58016d418401e5a0e8e9baef8d569514"
                val unit = sharedPreferences.getUnit()

                weatherRepository.getWeatherInfo2(latitude,longitude, apiKey, unit, language)
                    .flowOn(Dispatchers.IO)
                    .collect { weatherResponse ->
                        withContext(Dispatchers.Main) {
                            val currentTemp = weatherResponse.main.temp
                            //Toast.makeText(applicationContext, "Current Temperature: $currentTemp °C", Toast.LENGTH_SHORT).show()
                            setupNotification(alarmName, currentTemp)
                        }
                    }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    //Toast.makeText(applicationContext, "Failed to fetch weather data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun createNotification(): Notification {
        val channelId = "alarm_service_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alarm Service Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for Alarm Service notifications"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Alarm Service Running")
            .setContentText("Your alarm service is active.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun setupNotification(alarmName: String, currentTemp: Double) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "alarm_channel_id"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Alarm Channel", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Channel for alarm notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val dismissIntent = Intent(this, AlarmService::class.java).apply {
            action = "DISMISS_ALARM"
        }
        val dismissPendingIntent = PendingIntent.getService(this, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(this, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Alarm Triggered")
            .setContentText("Alarm: $alarmName\nCurrent Temperature: $currentTemp °C")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_launcher_foreground, "Dismiss", dismissPendingIntent)
            .addAction(R.drawable.alarm, "Open App", openAppPendingIntent)
            .build()
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun handleAlarmSound() {
        val sharedPreferences = SettingsManager(applicationContext)
        if (sharedPreferences.getNotificationType()) playAlarmSound()
    }

    private fun playAlarmSound() {
        mediaPlayer?.apply { start() }
    }

    private fun stopAlarmSound() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            mediaPlayer = null
        }
    }

    private fun getCityNameFromCoordinates(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(applicationContext, Locale.getDefault())
        val addresses: List<Address>? = try {
            geocoder.getFromLocation(latitude, longitude, 1)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        return addresses?.firstOrNull()?.locality ?: "Unknown City"
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarmSound()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
