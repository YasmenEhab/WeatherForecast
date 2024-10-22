package com.example.weatherforecastapplication.network

import com.example.weatherforecastapplication.model.ForecastResponse
import com.example.weatherforecastapplication.model.WeatherResponse
import kotlinx.coroutines.flow.Flow

interface WeatherRemoteDataSource {

    suspend  fun getWeatherInfoOverNetwork(city: String, apiKey: String, units: String):  Flow<WeatherResponse>
    suspend fun getWeatherForecastOverNetwork(city: String, apiKey: String, units: String): Flow<ForecastResponse>

    }