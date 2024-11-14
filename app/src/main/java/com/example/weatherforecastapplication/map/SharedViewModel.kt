package com.example.weatherforecastapplication.map

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _selectedCity = MutableLiveData<String>()
    val selectedCity: LiveData<String> get() = _selectedCity
    private val _selectedCoordinates = MutableLiveData<Pair<Double, Double>>()
    val selectedCoordinates: LiveData<Pair<Double, Double>> = _selectedCoordinates

    fun selectCoordinates(latitude: Double, longitude: Double) {
        _selectedCoordinates.value = Pair(latitude, longitude)
    }
    fun selectCity(cityName: String, latitude: Double, longitude: Double) {
        Log.d("SharedViewModel", "City selected: $cityName") // Log the selected city
        _selectedCity.value = cityName
    }
}