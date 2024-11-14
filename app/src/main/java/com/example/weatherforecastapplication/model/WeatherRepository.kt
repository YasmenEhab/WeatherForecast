package com.example.weatherforecastapplication.model

import com.example.weatherforecastapplication.alarm.view.Alarm
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    suspend fun getWeatherInfo(city: String, apiKey: String, units: String, lang: String): Flow<WeatherResponse>
    suspend fun getWeatherInfo2(lat: Double, long :Double, apiKey: String, units: String, lang: String): Flow<WeatherResponse>

    suspend fun getThreeHourForecast(city: String, apiKey: String, units: String, lang: String): Flow<ForecastResponse>
    suspend fun getThreeHourForecast2(lat: Double, long :Double, apiKey: String, units: String, lang: String): Flow<ForecastResponse>

    suspend fun getDailyForecast(city: String, apiKey: String, units: String, lang: String): Flow<ForecastResponse>
    suspend fun getDailyForecast2(lat: Double, long :Double, apiKey: String, units: String, lang: String): Flow<ForecastResponse>

    suspend fun saveFavoriteCity(city: FavoriteCity)
    suspend fun deleteFavoriteCity(cityId: Int)
    suspend fun getFavoriteCities(): Flow<List<FavoriteCity>>
    suspend fun insertAlarm(alarm: Alarm)
    suspend fun getAllAlarms(): List<Alarm>
    suspend fun deleteAlarm(alarm: Alarm)


}