package com.example.weatherforecastapplication.db

import android.util.Log
import com.example.weatherforecastapplication.alarm.view.Alarm
import com.example.weatherforecastapplication.model.FavoriteCity
import com.example.weatherforecastapplication.model.ForecastResponse
import com.example.weatherforecastapplication.model.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.math.pow
import kotlin.math.roundToInt

class LocalDataSourceImpl(private val favoriteCityDao: FavoriteCityDao, private val weatherDao: WeatherDao, private val forecastResponseDao: ForecastResponseDao) :
    LocalDataSource {
    private val TAG = "LocalDataSourceImpl"

    // Utility function to round latitude and longitude
    private fun Double.roundTo(decimals: Int): Double {
        val factor = 10.0.pow(decimals.toDouble())
        return (this * factor).roundToInt() / factor
    }

    // Insert a favorite city into the database
    override suspend fun saveFavoriteCity(city: FavoriteCity) {
        val roundedCity = city.copy(
            latitude = city.latitude.roundTo(4),
            longitude = city.longitude.roundTo(4)
        )
        try {
            favoriteCityDao.insertFavoriteCity(roundedCity)
            Log.d(TAG, "Favorite city saved successfully: ${roundedCity.cityName}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving favorite city: ${e.message}", e)
        }
    }

    // Retrieve all favorite cities from the database
    override suspend fun getFavoriteCities(): Flow<List<FavoriteCity>> {
        return favoriteCityDao.getAllFavoriteCities()
    }

    // Remove a specific favorite city by its ID
    override suspend fun deleteFavoriteCity(cityId: Int) {
        favoriteCityDao.deleteFavoriteCity(cityId)
    }

    override suspend fun addFavoriteCity(cityName: String) {

        val favoriteCity = FavoriteCity(cityName = cityName)
        try {
            Log.d(TAG, "Adding favorite city: $cityName")
            favoriteCityDao.insertFavoriteCity(favoriteCity)
            Log.d(TAG, "City $cityName added successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding city $cityName: ${e.message}", e)
        }
    }

    // Save weather data into the database
    override suspend fun saveWeatherData(weather: WeatherResponse) {
        try {
            // Round latitude and longitude before saving
            weather.coord.lat = weather.coord.lat.roundTo(4) // Edited: Round latitude
            weather.coord.lon = weather.coord.lon.roundTo(4) // Edited: Round longitude

            weatherDao.insertWeather(weather)
            Log.d(TAG, "Weather data saved successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving weather data: ${e.message}", e)
        }
    }

    // Retrieve weather data by city name
    override suspend fun getWeatherDataByCity(cityName: String): WeatherResponse? {
        return try {
            weatherDao.getWeatherByCityName(cityName)
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving weather data for city $cityName: ${e.message}", e)
            null
        }
    }

    override suspend fun getWeatherDataByCity2(lat: Double, long: Double): WeatherResponse? {
        return try {
            // Round latitude and longitude before querying
            val roundedLat = lat.roundTo(4) // Edited: Round latitude
            val roundedLong = long.roundTo(4) // Edited: Round longitude

            val result = weatherDao.getWeatherByCityName2(roundedLat, roundedLong)
            Log.d(TAG, "Query result for geo: $roundedLat and $roundedLong -> $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving weather data for geo: $lat and $long: ${e.message}", e)
            null
        }
    }

    // Retrieve all saved weather data
    override suspend fun getAllWeatherData(): List<WeatherResponse> {
        return try {
            weatherDao.getAllWeatherData()
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving all weather data: ${e.message}", e)
            emptyList()
        }
    }

    // Clear all weather data from the database
    override suspend fun clearAllWeatherData() {
        try {
            weatherDao.clearAllWeatherData()
            Log.d(TAG, "All weather data cleared.")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing all weather data: ${e.message}", e)
        }
    }

    // Retrieve all forecast data
    override suspend fun saveForecastData(forecastResponse: ForecastResponse) {
        forecastResponseDao.insertForecastResponse(forecastResponse)
    }

    override suspend fun getLatestForecastData(): ForecastResponse? {
        return forecastResponseDao.getLatestForecastResponse()
    }

    override suspend fun insertAlarm(alarm: Alarm) = withContext(Dispatchers.IO) {
        weatherDao.insertAlarm(alarm)
    }

    override suspend fun getAllAlarms(): List<Alarm> {
        return weatherDao.getAllAlarms()
    }

    override suspend fun deleteAlarm(alarm: Alarm) {
        weatherDao.deleteAlarm(alarm)
    }
}