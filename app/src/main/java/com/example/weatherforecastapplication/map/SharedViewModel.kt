package com.example.weatherforecastapplication.map

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _selectedCity = MutableLiveData<String>()
    val selectedCity: LiveData<String> get() = _selectedCity

    fun selectCity(cityName: String) {
        Log.d("SharedViewModel", "City selected: $cityName") // Log the selected city
        _selectedCity.value = cityName
    }
}