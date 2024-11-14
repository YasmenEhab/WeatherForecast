package com.example.weatherforecastapplication.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.OnConflictStrategy
import com.example.weatherforecastapplication.alarm.view.Alarm

import com.example.weatherforecastapplication.model.WeatherResponse
@Dao
interface  WeatherDao {
    // Insert or replace weather data
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherResponse)

    // Update existing weather data
    @Update
    suspend fun updateWeather(weather: WeatherResponse)

    // Get weather data by city name (if the name is unique or required)
    @Query("SELECT * FROM weather_table WHERE name = :cityName LIMIT 1")
    suspend fun getWeatherByCityName(cityName: String): WeatherResponse?

    @Query("SELECT * FROM weather_table WHERE ROUND(coord_lon, 4) = ROUND(:longitude, 4) AND ROUND(coord_lat, 4) = ROUND(:latitude, 4) LIMIT 1")
    suspend fun getWeatherByCityName2(latitude: Double, longitude: Double): WeatherResponse?

    // Get all weather data (useful if you have multiple entries)
    @Query("SELECT * FROM weather_table")
    suspend fun getAllWeatherData(): List<WeatherResponse>

    // Delete weather data by city name
    @Query("DELETE FROM weather_table WHERE name = :cityName")
    suspend fun deleteWeatherByCityName(cityName: String)

    // Optional: Clear all weather data
    @Query("DELETE FROM weather_table")
    suspend fun clearAllWeatherData()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAlarm(alarm: Alarm)

    @Query("SELECT * FROM alarms")
    suspend fun getAllAlarms(): List<Alarm>

    @Delete
    suspend fun deleteAlarm(alarm: Alarm)
}