package com.example.weatherforecastapplication.db

import com.example.weatherforecastapplication.alarm.view.Alarm
import com.example.weatherforecastapplication.model.FavoriteCity
import com.example.weatherforecastapplication.model.ForecastResponse
import com.example.weatherforecastapplication.model.WeatherResponse
import kotlinx.coroutines.flow.Flow


interface LocalDataSource {

    suspend fun saveFavoriteCity(city: FavoriteCity)
    suspend fun getFavoriteCities(): Flow<List<FavoriteCity>>
    suspend fun deleteFavoriteCity(cityId: Int)
    suspend fun addFavoriteCity(cityName: String)

    // Weather data methods
    suspend fun saveWeatherData(weather: WeatherResponse)
    suspend fun getWeatherDataByCity(cityName: String): WeatherResponse?
    suspend fun getWeatherDataByCity2(lat :Double , long:Double): WeatherResponse?
    suspend fun getAllWeatherData(): List<WeatherResponse>
    suspend fun clearAllWeatherData()

    // Forecast data methods
    suspend fun saveForecastData(forecastResponse: ForecastResponse)
     suspend fun getLatestForecastData(): ForecastResponse?

    suspend fun deleteAlarm(alarm: Alarm)
    suspend fun insertAlarm(alarm: Alarm)
    suspend fun getAllAlarms(): List<Alarm>

}