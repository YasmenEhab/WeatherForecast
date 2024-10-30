package com.example.weatherforecastapplication.home.viewmodel

import com.example.weatherforecastapplication.model.ForecastResponse
import com.example.weatherforecastapplication.model.WeatherResponse


sealed class ApiState {
    data class Success(val weatherResponse: WeatherResponse) : ApiState()
    data class Failure(val message: String) : ApiState()
    object Loading : ApiState()
}






