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
    override suspend fun getThreeHourForecast(city: String, apiKey: String, units: String, lang: String): Flow<ForecastResponse> {
        return remoteDataSource.getWeatherForecastOverNetwork(city, apiKey, units, lang).map { forecastResponse ->
            // Take the next 8 forecasts in 3-hour intervals
            val nextEightForecasts = forecastResponse.list.take(8) // Adjust to ensure we get the first 8 items

            // Create a new ForecastResponse with these 8 forecasts
            ForecastResponse(
                list = nextEightForecasts,
                city = forecastResponse.city // Retaining the city information
            )
        }
    }

    // Get daily temperatures by grouping forecast into days
    override suspend fun getDailyForecast(city: String, apiKey: String, units: String, lang: String): Flow<ForecastResponse> {
        return remoteDataSource.getWeatherForecastOverNetwork(city, apiKey, units, lang).map { forecastResponse ->
            // Group by day (86400 seconds = 1 day)
            val groupedForecasts = forecastResponse.list.groupBy { it.dt / 86400 }
            // Take the first forecast for each of the first 5 unique days
            val dailyForecasts = groupedForecasts.entries.take(5).flatMap { entry -> entry.value.take(1) } // Adjusted to ensure only one entry per day
            // Create a new ForecastResponse with the daily forecasts
            ForecastResponse(
                list = dailyForecasts,
                city = forecastResponse.city // Retaining the city information
            )}

    }

}