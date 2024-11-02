package com.example.weatherforecastapplication.fav.viewmodel

import com.example.weatherforecastapplication.model.FavoriteCity
import com.example.weatherforecastapplication.model.WeatherResponse

sealed class FavState {
    data class Success(val weatherResponse: List<FavoriteCity>) : FavState()
    data class Failure(val message: String) : FavState()
    object Loading : FavState()

}

