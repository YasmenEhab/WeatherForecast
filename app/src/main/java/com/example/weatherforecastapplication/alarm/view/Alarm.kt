package com.example.weatherforecastapplication.alarm.view

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val time: String,
    val hour: Int,
    val minute: Int,
    val latitude: Double,
    val longitude: Double
)
{
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as Alarm
        return name == other.name && time == other.time && latitude == other.latitude && longitude == other.longitude
    }

    override fun hashCode(): Int {
        return 31 * name.hashCode() + time.hashCode() + latitude.hashCode() + longitude.hashCode()
    }
}
