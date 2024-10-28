package com.example.weatherforecastapplication.model


import com.example.weatherforecastapplication.network.WeatherRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class WeatherRepositoryImpl private constructor(
    private val remoteDataSource: WeatherRemoteDataSource
) : WeatherRepository {

    companion object {
        @Volatile
        private var INSTANCE: WeatherRepositoryImpl? = null
        fun getInstance(
            remoteDataSource: WeatherRemoteDataSource
        ): WeatherRepositoryImpl {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WeatherRepositoryImpl(
                    remoteDataSource
                ).also { INSTANCE = it }
            }

        }
    }
    // Get current weather data from the remote data source
    override suspend fun getWeatherInfo(city: String, apiKey: String, units: String, lang: String): Flow<WeatherResponse> {

            return remoteDataSource.getWeatherInfoOverNetwork(city , apiKey ,units , lang )

    }

    // Get 3-hour interval temperatures and process them
    override suspend fun getThreeHourForecast(city: String, apiKey: String, units: String, lang: String): Flow<List<Forecast>> {
        return remoteDataSource.getWeatherForecastOverNetwork(city, apiKey, units , lang).map { forecastResponse ->
            // Extract and return the list of 3-hour forecast
            forecastResponse.list
        }
    }

    // Get daily temperatures by grouping forecast into days
    override suspend fun getDailyForecast(city: String, apiKey: String, units: String, lang: String): Flow<List<Forecast>> {
        return remoteDataSource.getWeatherForecastOverNetwork(city, apiKey, units, lang).map { forecastResponse ->
            // Group by day and calculate daily temperatures
            forecastResponse.list.groupBy { it.dt / 86400 } // Group by day (86400 seconds = 1 day)
                .map { entry -> entry.value.first() } // For simplicity, taking the first forecast of each day
        }
    }

}