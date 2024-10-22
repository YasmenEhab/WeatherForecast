// ProductsRemoteDataSourceImpl.kt
package com.example.weatherforecastapplication.network

import com.example.weatherforecastapplication.model.ForecastResponse
import com.example.weatherforecastapplication.model.WeatherResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class WeatherRemoteDataSourceImpl(private val apiService: WeatherService) : WeatherRemoteDataSource {

    /* make function return Flow and and emit  */
    override suspend fun getWeatherInfoOverNetwork(city: String, apiKey: String, units: String): Flow<WeatherResponse> {
        return flow {
            try {
                // Make the API call to get the weather information
                val response = apiService.getCurrentWeather(city, apiKey, units)

                // Emit the result to the flow
                emit(response)
            } catch (e: Exception) {
                // Handle any exceptions (e.g., network issues) and emit an empty object or handle errors accordingly
                throw e // You can also handle it with custom error handling
            }
        }
    }
    /* Fetch weather forecast for the next 5 days and emit as Flow */
    override suspend fun getWeatherForecastOverNetwork(city: String, apiKey: String, units: String): Flow<ForecastResponse> {
        return flow {
            try {
                // Make the API call to get the weather forecast
                val response = apiService.getWeatherForecast(city, apiKey, units)

                // Emit the result to the flow
                emit(response)
            } catch (e: Exception) {
                // Handle any exceptions (e.g., network issues)
                throw e // Custom error handling can be implemented here
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: WeatherRemoteDataSourceImpl? = null

        fun getInstance(apiService: WeatherService): WeatherRemoteDataSourceImpl {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WeatherRemoteDataSourceImpl(apiService).also { INSTANCE = it }
            }
        }
    }
}
