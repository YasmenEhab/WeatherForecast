package com.example.weatherforecastapplication.home.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecastapplication.model.Forecast
import com.example.weatherforecastapplication.model.WeatherRepository
import com.example.weatherforecastapplication.model.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repo: WeatherRepository) : ViewModel() {

    private val _weatherData = MutableStateFlow<ApiState>(ApiState.Loading)
    val weatherData: StateFlow<ApiState> = _weatherData

    // StateFlow for 3-hour forecast data
    private val _threeHourForecast = MutableStateFlow<List<Forecast>?>(null)
    val threeHourForecast: StateFlow<List<Forecast>?> = _threeHourForecast

    // StateFlow for daily forecast data
    private val _dailyForecast = MutableStateFlow<List<Forecast>?>(null)
    val dailyForecast: StateFlow<List<Forecast>?> = _dailyForecast

    //private val apiKey = "7135fce85546c3c812b6e29c55b879cf"
    private val apiKey = "58016d418401e5a0e8e9baef8d569514"


    // Fetch current weather
    fun fetchWeather(city: String, units: String) {
        _weatherData.value = ApiState.Loading // Set to Loading before fetching data
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Fetch weather from repository and collect the flow
                repo.getWeatherInfo(city, apiKey, units).collect { weatherResponse ->
                    Log.d("HomeViewModel", "Current weather: $weatherResponse")
                    // Emit the weather response to the StateFlow
                    _weatherData.value = ApiState.Success(weatherResponse)                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching weather: ${e.message}")
                _weatherData.value = ApiState.Failure(e.message ?: "An unknown error occurred")

            }
        }
    }

    // Fetch 3-hour forecast data
    fun fetchThreeHourForecast(city: String, units: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Fetch 3-hour forecast from repository and collect the flow
                repo.getThreeHourForecast(city, apiKey, units).collect { forecastList ->
                    Log.d("HomeViewModel", "3-hour forecast: $forecastList")
                    // Emit the 3-hour forecast to the StateFlow
                    _threeHourForecast.value = forecastList
                }
            } catch (e: Exception) {
                // Handle exceptions
            }
        }
    }

    // Fetch daily forecast data
    fun fetchDailyForecast(city: String, units: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Fetch daily forecast from repository and collect the flow
                repo.getDailyForecast(city, apiKey, units).collect { dailyList ->
                    Log.d("HomeViewModel", "Daily forecast: $dailyList")
                    // Emit the daily forecast to the StateFlow
                    _dailyForecast.value = dailyList
                }
            } catch (e: Exception) {
                // Handle exceptions
            }
        }
    }
}