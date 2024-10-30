package com.example.weatherforecastapplication.model

import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    suspend fun getWeatherInfo(city: String, apiKey: String, units: String, lang: String): Flow<WeatherResponse>
    suspend fun getThreeHourForecast(city: String, apiKey: String, units: String, lang: String): Flow<ForecastResponse>
    suspend fun getDailyForecast(city: String, apiKey: String, units: String, lang: String): Flow<ForecastResponse>

}