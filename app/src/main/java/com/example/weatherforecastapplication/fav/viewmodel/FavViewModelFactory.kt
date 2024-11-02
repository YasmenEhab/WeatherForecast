package com.example.weatherforecastapplication.fav.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecastapplication.home.viewmodel.HomeViewModel
import com.example.weatherforecastapplication.model.WeatherRepository

class FavViewModelFactory (private val weatherRepository: WeatherRepository, private val sharedPreferences: SharedPreferences) : ViewModelProvider.Factory{
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the ViewModel class matches HomeViewModel
        if (modelClass.isAssignableFrom(FavViewModel::class.java)){
            return  FavViewModel(weatherRepository ,  sharedPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}

