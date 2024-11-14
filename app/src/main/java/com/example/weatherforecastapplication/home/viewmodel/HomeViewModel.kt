package com.example.weatherforecastapplication.home.viewmodel

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.model.Forecast
import com.example.weatherforecastapplication.model.WeatherRepository
import com.example.weatherforecastapplication.model.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class HomeViewModel(private val repo: WeatherRepository ,  private val sharedPreferences: SharedPreferences) : ViewModel() {

    private val _weatherData = MutableStateFlow<ApiState>(ApiState.Loading)
    val weatherData: StateFlow<ApiState> = _weatherData

    private val _weatherIconResource = MutableStateFlow<Int?>(null) // StateFlow for weather icon
    val weatherIconResource: StateFlow<Int?> = _weatherIconResource

    // StateFlow for 3-hour forecast data
    private val _threeHourForecast = MutableStateFlow<ForecastState>(ForecastState.Loading)
    val threeHourForecast: StateFlow<ForecastState> = _threeHourForecast

    // StateFlow for daily forecast data
    private val _dailyForecast = MutableStateFlow<ForecastState>(ForecastState.Loading)
    val dailyForecast: StateFlow<ForecastState> = _dailyForecast

    //private val apiKey = "7135fce85546c3c812b6e29c55b879cf"
    private val apiKey = "58016d418401e5a0e8e9baef8d569514"


    private var currentCity: String? = null // Keep track of the city

    init {
        loadPreferencesAndFetchWeather()
    }

    // Load preferences and fetch weather data
    private fun loadPreferencesAndFetchWeather() {
        val units = sharedPreferences.getString("TEMPERATURE_UNIT", "metric") ?: "metric"
        val lang = sharedPreferences.getString("LANGUAGE", "en") ?: "en"

//        fetchWeather(city, units, lang)
//        fetchThreeHourForecast(city, units, lang)
//        fetchDailyForecast(city, units, lang)
    }

    // Fetch current weather
    fun fetchWeather(city: String, units: String, lang: String) {
        if (city == currentCity) {
            Log.d("HomeViewModel", "Same city request detected; skipping fetch.")
            return // Skip if city hasn't changed
        }
        currentCity = city
        _weatherData.value = ApiState.Loading // Set to Loading before fetching data
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Fetch weather from repository and collect the flow
                repo.getWeatherInfo(city, apiKey, units,lang).collect { weatherResponse ->
                    Log.d("HomeViewModel", "Current weather: $weatherResponse")
                    // Emit the weather response to the StateFlow
                    _weatherData.value = ApiState.Success(weatherResponse)
                    updateWeatherIcon(weatherResponse)}
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching weather: ${e.message}")
                _weatherData.value = ApiState.Failure(e.message ?: "An unknown error occurred")

            }
        }
    }
    fun fetchWeather2(lat :Double,long:Double, units: String, lang: String) {

        _weatherData.value = ApiState.Loading // Set to Loading before fetching data
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Fetch weather from repository and collect the flow
                repo.getWeatherInfo2(lat, long, apiKey, units,lang).collect { weatherResponse ->
                    Log.d("HomeViewModel", "Current weather: $weatherResponse")
                    // Emit the weather response to the StateFlow
                    _weatherData.value = ApiState.Success(weatherResponse)
                    updateWeatherIcon(weatherResponse)}
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching weather: ${e.message}")
                _weatherData.value = ApiState.Failure(e.message ?: "An unknown error occurred")

            }
        }
    }

    // Fetch 3-hour forecast data
    fun fetchThreeHourForecast(city: String, units: String, lang: String) {
        _threeHourForecast.value=ForecastState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Fetch 3-hour forecast from repository and collect the flow
                repo.getThreeHourForecast(city, apiKey, units, lang).collect { forecastResponse ->
                    Log.d("HomeViewModel", "3-hour forecast: $forecastResponse")
                    // Emit the 3-hour forecast to the StateFlow
                    _threeHourForecast.value = ForecastState.Success(forecastResponse)

                }
            } catch (e: Exception) {
                _threeHourForecast.value=ForecastState.Failure(e.message ?: "An unknown error occurred")
            }
        }
    }
    fun fetchThreeHourForecast2(lat :Double,long:Double, units: String, lang: String) {
        _threeHourForecast.value=ForecastState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Fetch 3-hour forecast from repository and collect the flow
                repo.getThreeHourForecast2(lat,long, apiKey, units, lang).collect { forecastResponse ->
                    Log.d("HomeViewModel", "3-hour forecast: $forecastResponse")
                    // Emit the 3-hour forecast to the StateFlow
                    _threeHourForecast.value = ForecastState.Success(forecastResponse)

                }
            } catch (e: Exception) {
                _threeHourForecast.value=ForecastState.Failure(e.message ?: "An unknown error occurred")
            }
        }
    }

    // Fetch daily forecast data
    fun fetchDailyForecast(city: String, units: String, lang: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Fetch daily forecast from repository and collect the flow
                repo.getDailyForecast(city, apiKey, units, lang).collect { forecastResponse ->
                    Log.d("HomeViewModel", "Daily forecast: $forecastResponse")
                    // Emit the daily forecast to the StateFlow
                    _dailyForecast.value = ForecastState.Success(forecastResponse)
                }
            } catch (e: Exception) {
               _dailyForecast.value = ForecastState.Failure(e.message ?:"An unknown error occurred")
            }
        }
    }
    fun fetchDailyForecast2(lat :Double,long:Double, units: String, lang: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Fetch daily forecast from repository and collect the flow
                repo.getDailyForecast2(lat,long, apiKey, units, lang).collect { forecastResponse ->
                    Log.d("HomeViewModel", "Daily forecast: $forecastResponse")
                    // Emit the daily forecast to the StateFlow
                    _dailyForecast.value = ForecastState.Success(forecastResponse)
                }
            } catch (e: Exception) {
                _dailyForecast.value = ForecastState.Failure(e.message ?:"An unknown error occurred")
            }
        }
    }

    // Function to determine the weather icon
    private fun updateWeatherIcon(weatherResponse: WeatherResponse) {
        val iconResId = when (weatherResponse.weather.firstOrNull()?.main) { // Adjust as necessary
            "Clear" -> R.drawable.sun
            "Clouds" -> R.drawable.cloudy_3
            "Rain" -> R.drawable.rainy
            "Snow" -> R.drawable.snowy
            "Dust" -> R.drawable.wind
            else -> R.drawable.sun // Fallback icon
        }
        _weatherIconResource.value = iconResId // Update the StateFlow with the new icon resource
    }

}