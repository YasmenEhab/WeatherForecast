package com.example.weatherforecastapplication.alarm.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.Manifest
import androidx.lifecycle.LiveData
import android.provider.Settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weatherforecastapplication.alarm.broadcast.AlarmReceiver
import com.example.weatherforecastapplication.LocationGetter
import com.example.weatherforecastapplication.alarm.view.Alarm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class AlarmViewModel(private val context: Context) : ViewModel() {
    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // LiveData to keep track of alarms
    private val _alarms = MutableLiveData<MutableList<Alarm>>(mutableListOf())
    val alarms: LiveData<MutableList<Alarm>> = _alarms

    fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                context.startActivity(intent)
            }
        }
    }

    fun setAlarm(alarm: Alarm) {
        if (!alarm.isActive) return
        val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarmName", alarm.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(), // Use the unique ID for each alarm
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarm.timeInMillis, pendingIntent)
            } else {
                // Handle permission not granted case
            }
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarm.timeInMillis, pendingIntent)
        }

        addAlarm(alarm) // Add the alarm to the list after setting
    }

    fun cancelAlarm(alarm: Alarm) {
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(), // Match the ID for cancellation
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)

        // Remove the alarm from the list
        _alarms.value?.remove(alarm)
        _alarms.value = _alarms.value // Update LiveData
    }

    fun isExactAlarmPermissionGranted(): Boolean {
        return context.checkSelfPermission(Manifest.permission.SCHEDULE_EXACT_ALARM) == PackageManager.PERMISSION_GRANTED
    }

    fun scheduleAlarm(hour: Int, minute: Int, name: String) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)

            // Adjust for next day if the time is in the past
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        // Create a unique ID for the alarm, here we can use the current time for simplicity
        val id = calendar.timeInMillis.toInt() // Simple way to generate a unique ID

        // Create the Alarm object with the time in milliseconds
        val alarm = Alarm(id.toLong(), name, calendar.timeInMillis.toString(), calendar.timeInMillis, true) // Now includes timeInMillis
        setAlarm(alarm) // Call setAlarm with the Alarm object
    }


    fun getLocation(callback: (Double?, Double?) -> Unit) {
        val locationGetter = LocationGetter(context)
        CoroutineScope(Dispatchers.Main).launch {
            val location = locationGetter.getLocation()
            callback(location?.latitude, location?.longitude)
        }
    }

     fun addAlarm(alarm: Alarm) {
         val currentAlarms = _alarms.value ?: mutableListOf()
         currentAlarms.add(alarm)
         _alarms.value = currentAlarms // Notify observers
     }
    // Remove an alarm from the LiveData list
    private fun removeAlarm(alarm: Alarm) {
        val currentAlarms = _alarms.value ?: mutableListOf()
        currentAlarms.remove(alarm)
        _alarms.value = currentAlarms // Notify observers
    }
}
