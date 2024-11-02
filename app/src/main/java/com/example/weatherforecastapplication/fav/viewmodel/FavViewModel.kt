package com.example.weatherforecastapplication.fav.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecastapplication.home.viewmodel.ApiState
import com.example.weatherforecastapplication.home.viewmodel.ForecastState
import com.example.weatherforecastapplication.model.FavoriteCity
import com.example.weatherforecastapplication.model.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavViewModel(
    private val repo: WeatherRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {
    private val _weatherData = MutableStateFlow<FavState>(FavState.Loading)
    val weatherData: StateFlow<FavState> = _weatherData


    //private val apiKey = "7135fce85546c3c812b6e29c55b879cf"
    private val apiKey = "58016d418401e5a0e8e9baef8d569514"


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

    fun getAllFavCities() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getFavoriteCities().collect { favoriteCities ->
                _weatherData.value = FavState.Success(favoriteCities)
            }
        }
    }

    fun deleteFavCity(city: FavoriteCity) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.deleteFavoriteCity(city.id)// After deletion, fetch the updated list of favorite cities
            repo.getFavoriteCities().collect { favoriteCities ->
                _weatherData.value = FavState.Success(favoriteCities)
            }
        }
    }
}
