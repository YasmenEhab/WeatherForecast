package com.example.weatherforecastapplication.network

import com.example.weatherforecastapplication.model.ForecastResponse
import com.example.weatherforecastapplication.model.WeatherResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("q") city: String,             // City name (e.g., "London")
        @Query("appid") apiKey: String,      // API key for authentication
        @Query("units") units: String,       // Units (metric, imperial, etc.)
        @Query("lang") lang: String          // Language (e.g., "en" for English, "ar" for Arabic)

    ): WeatherResponse

    @GET("data/2.5/weather")
    suspend fun getCurrentWeather2(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,      // API key for authentication
        @Query("units") units: String,       // Units (metric, imperial, etc.)
        @Query("lang") lang: String          // Language (e.g., "en" for English, "ar" for Arabic)

    ): WeatherResponse

    @GET("data/2.5/forecast")
    suspend fun getWeatherForecast(
        @Query("q") city: String,
        @Query("appid") appId: String,
        @Query("units") units: String,   // Units (metric, imperial, etc.)
        @Query("lang") lang: String          // Language (e.g., "en" for English, "ar" for Arabic)

    ): ForecastResponse

    @GET("data/2.5/forecast")
    suspend fun getWeatherForecast2(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") appId: String,
        @Query("units") units: String,   // Units (metric, imperial, etc.)
        @Query("lang") lang: String          // Language (e.g., "en" for English, "ar" for Arabic)

    ): ForecastResponse


}