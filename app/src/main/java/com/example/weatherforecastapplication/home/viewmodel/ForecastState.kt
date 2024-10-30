package com.example.weatherforecastapplication.home.viewmodel

import com.example.weatherforecastapplication.model.ForecastResponse

sealed class ForecastState {
    data class Success(val forecastResponse: ForecastResponse) : ForecastState()
    data class Failure(val message: String) : ForecastState()
    object Loading : ForecastState()
}