package com.example.weatherforecastapplication.model

import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    suspend fun getProducts(city: String, apiKey: String, units: String): Flow<WeatherResponse>
}