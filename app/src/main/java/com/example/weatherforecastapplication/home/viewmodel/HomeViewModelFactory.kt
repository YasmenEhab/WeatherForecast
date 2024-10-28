package com.example.weatherforecastapplication.home.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecastapplication.model.WeatherRepository

class HomeViewModelFactory(private val weatherRepository: WeatherRepository ,  private val sharedPreferences: SharedPreferences) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the ViewModel class matches HomeViewModel
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(weatherRepository ,  sharedPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}