package com.example.weatherforecastapplication.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitHelper {
    // Base URL for OpenWeatherMap API
    private const val BASE_URL = "https://api.openweathermap.org/"

    // Use a function to get the Retrofit instance
    fun getInstance(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
