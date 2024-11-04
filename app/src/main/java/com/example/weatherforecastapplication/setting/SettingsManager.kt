package com.example.weatherforecastapplication.setting

import android.content.Context

class SettingsManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
    fun getLanguage(): String {
        return sharedPreferences.getString("LANGUAGE", "en") ?: "en"
    }

    fun getTemperatureUnit(): String {
        return sharedPreferences.getString("TEMPERATURE_UNIT", "metric") ?: "metric"
    }

    fun getLocationOption(): String {
        return sharedPreferences.getString("LOCATION_OPTION", "gps") ?: "gps"
    }

}