package com.example.weatherforecastapplication.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecastapplication.model.WeatherRepository
import com.example.weatherforecastapplication.model.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repo: WeatherRepository) : ViewModel() {

    private val _weatherData = MutableStateFlow<WeatherResponse?>(null)
    val weatherData: StateFlow<WeatherResponse?> = _weatherData

    private val apiKey = "7135fce85546c3c812b6e29c55b879cf"

    fun fetchWeather(city: String, units: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Fetch weather from repository and collect the flow
                repo.getProducts(city, apiKey, units).collect { weatherResponse ->
                    // Emit the weather response to the StateFlow
                    _weatherData.value = weatherResponse
                }
            } catch (e: Exception) {
                // Handle exceptions (e.g., show error message to UI)
            }
        }
    }
}