package com.example.weatherforecastapplication.alarm.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.Manifest
import androidx.lifecycle.LiveData
import android.provider.Settings
import android.util.Log

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weatherforecastapplication.alarm.broadcast.AlarmReceiver
import com.example.weatherforecastapplication.LocationGetter
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.alarm.view.Alarm
import com.example.weatherforecastapplication.home.viewmodel.ApiState
import com.example.weatherforecastapplication.model.WeatherRepository
import com.example.weatherforecastapplication.model.WeatherResponse
import com.example.weatherforecastapplication.setting.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class AlarmViewModel(
    private val weatherRepository: WeatherRepository,
    context: Context
) : ViewModel() {
    private var currentCity: String? = null // Keep track of the city
    private val apiKey = "58016d418401e5a0e8e9baef8d569514"
    private val _weatherIconResource = MutableStateFlow<Int?>(null) // StateFlow for weather icon
    private var isFetchingAlarms = false
    private val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val sharedPreferences = SettingsManager(context)

    private val _alarms = MutableLiveData<MutableList<Alarm>>(mutableListOf())
    val alarms: LiveData<MutableList<Alarm>> = _alarms

    private val _weatherData = MutableStateFlow<ApiState>(ApiState.Loading)
    val weatherData: StateFlow<ApiState> = _weatherData

    private val language: String = sharedPreferences.getLanguage() ?: "en"
    private val unit: String = sharedPreferences.getUnit()

    // Map to store PendingIntents associated with each alarm
    private val pendingIntentMap = mutableMapOf<Int, PendingIntent>()

    // Function to add and store an alarm in the LiveData and repository
    fun addAlarm(alarm: Alarm) {
        viewModelScope.launch {
            try {

                val currentAlarms = _alarms.value ?: mutableListOf()
                if (!currentAlarms.contains(alarm)) {
                    currentAlarms.add(alarm)
                    _alarms.value = currentAlarms
                    insertAlarm(alarm) // Insert alarm into the repository
                }


            } catch (e: Exception) {
                Log.e("AlarmViewModel", "Error adding alarm: ${e.message}")
            }
        }
    }

    // Insert alarm into the repository
     suspend fun insertAlarm(alarm: Alarm) {
        viewModelScope.launch {
            weatherRepository.insertAlarm(alarm)
            fetchAlarms()
        }
    }

    // Fetch all alarms from the repository
    fun fetchAlarms() {
        if (isFetchingAlarms) return
        isFetchingAlarms = true
        Log.d("AlarmViewModel", "Fetching alarms")
        viewModelScope.launch {
            _alarms.value = weatherRepository.getAllAlarms().toMutableList()
            isFetchingAlarms = false
        }
    }

    // Fetch current weather based on location
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
                weatherRepository.getWeatherInfo(city, apiKey, units,lang).collect { weatherResponse ->
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
                weatherRepository.getWeatherInfo2(lat,long, apiKey, units,lang).collect { weatherResponse ->
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


    // Cancel a scheduled alarm and remove it from LiveData and repository
    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            weatherRepository.deleteAlarm(alarm) // Delete from the database
            alarms.value?.let {
                val updatedAlarms = it.toMutableList().apply { remove(alarm) }
                _alarms.value = updatedAlarms // Update LiveData
                // Optionally, notify adapter here if using a local adapter
            }

            // Cancel the PendingIntent associated with the alarm
            val requestCode = alarm.name.hashCode() // Assuming the name is unique
            pendingIntentMap[requestCode]?.cancel() // Cancel the PendingIntent
            pendingIntentMap.remove(requestCode) // Remove it from the map
        }
    }

    // Save a PendingIntent for tracking purposes
     fun savePendingIntent(requestCode: Int, pendingIntent: PendingIntent) {
        pendingIntentMap[requestCode] = pendingIntent
    }
}

// ViewModel factory with WeatherRepository injection
class AlarmViewModelFactory(
    private val weatherRepository: WeatherRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlarmViewModel::class.java)) {
            return AlarmViewModel(weatherRepository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
