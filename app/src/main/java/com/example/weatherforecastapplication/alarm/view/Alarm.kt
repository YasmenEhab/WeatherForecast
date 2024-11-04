package com.example.weatherforecastapplication.alarm.view

data class Alarm(
    val id: Long,             // Unique identifier for the alarm
    val name: String,
    val time: String,
    val timeInMillis: Long,   // New property to store the alarm time in milliseconds

    var isActive: Boolean     // Whether the alarm is active

)
{
    fun getFormattedTime(): String {
        val hours = time.split(":")[0].toInt()
        val minutes = time.split(":")[1].toInt()
        val period = if (hours < 12) "AM" else "PM"
        val formattedHour = if (hours % 12 == 0) 12 else hours % 12
        return String.format("%02d:%02d %s", formattedHour, minutes, period)
    }
}
