package com.example.weatherforecastapplication.model


import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.weatherforecastapplication.alarm.view.Alarm
import com.example.weatherforecastapplication.db.LocalDataSource
import com.example.weatherforecastapplication.network.WeatherRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart


class WeatherRepositoryImpl private constructor(
    val remoteDataSource: WeatherRemoteDataSource,
    private val localDataSource: LocalDataSource,
    private val context: Context // Required for network checking
) : WeatherRepository {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: WeatherRepositoryImpl? = null
        fun getInstance(
            remoteDataSource: WeatherRemoteDataSource,
            localDataSource: LocalDataSource,
            context: Context
        ): WeatherRepositoryImpl {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WeatherRepositoryImpl(
                    remoteDataSource,
                    localDataSource,
                    context
                ).also { INSTANCE = it }
            }

        }
    }

    // Get current weather data from the remote data source
    override suspend fun getWeatherInfo(
        city: String,
        apiKey: String,
        units: String,
        lang: String
    ): Flow<WeatherResponse> {

        return if (isNetworkAvailable()) {
            try {
                Log.d("WeatherRepository", "Network available. Fetching data from API for city: $city")

                val apiResponse = remoteDataSource.getWeatherInfoOverNetwork(city, apiKey, units, lang)

                // Map the API response and save it to the local database
                flow {
                    apiResponse.collect { weatherResponse ->
                        Log.d("WeatherRepository", "Saving API response to local database for city: $city")
                        localDataSource.saveWeatherData(weatherResponse) // Save to local database
                        emit(weatherResponse) // Emit the response
                    }
                }

            } catch (e: Exception) {
                Log.e(
                    "WeatherRepository",
                    "API request failed. Fetching last known data from database for city: $city",
                    e
                )

                // If API call fails, fallback to local database
                fetchWeatherFromLocal(city)
            }
        } else {
            Log.w("WeatherRepository", "No internet connection. Fetching data from local database for city: $city")
            fetchWeatherFromLocal(city)  // Fallback to local data if no internet
        }.onStart {
            Log.d("WeatherRepository", "Starting data fetch process for city: $city")
        }.catch { e ->
            Log.e("WeatherRepository", "Error encountered during data fetch process for city: $city", e)
            throw e
        }
    }
    override suspend fun getWeatherInfo2(
        lat: Double,
        long: Double,
        apiKey: String,
        units: String,
        lang: String
    ): Flow<WeatherResponse> {

        return if (isNetworkAvailable()) {
            try {
                Log.d("WeatherRepository", "Network available. Fetching data from API for geo: $lat and $long")

                val apiResponse: Flow<WeatherResponse> = remoteDataSource.getWeatherInfoOverNetwork2(lat, long, apiKey, units, lang)

                // Map the API response and save it to the local database
                flow { // Specify Flow<WeatherResponse> here to help with type inference
                    apiResponse.collect { weatherResponse ->
                        Log.d("WeatherRepository", "Saving API response to local database for geo: $lat and $long")
                        localDataSource.saveWeatherData(weatherResponse) // Save to local database
                        emit(weatherResponse) // Emit the response
                    }
                }

            } catch (e: Exception) {
                Log.e(
                    "WeatherRepository",
                    "API request failed. Fetching last known data from database for geo: $lat and $long",
                    e
                )

                // If API call fails, fallback to local database
                fetchWeatherFromLocal2(lat, long)
            }
        } else {
            Log.w("WeatherRepository", "No internet connection. Fetching data from local database for geo: $lat and $long")
            fetchWeatherFromLocal2(lat, long)  // Fallback to local data if no internet
        }.onStart {
            Log.d("WeatherRepository", "Starting data fetch process for geo: $lat and $long")
        }.catch { e ->
            Log.e("WeatherRepository", "Error encountered during data fetch process for geo: $lat and $long", e)
            throw e
        }
    }


    private fun fetchWeatherFromLocal2(lat: Double, long: Double): Flow<WeatherResponse> {
        return flow {
            val cachedData = localDataSource.getWeatherDataByCity2(lat, long)
            Log.d("WeatherRepository", "Cached data for geo: $lat and $long -> $cachedData")

            if (cachedData != null) {
                emit(cachedData) // Emit the cached data if available
                Log.d("WeatherRepository", "Returning cached data for geo: $lat and $long")
            } else {
                Log.e("WeatherRepository", "No local data available for geo: $lat and $long")
                throw Exception("No internet and no local data available for geo: $lat and $long")
            }
        }
    }

    // Helper function to fetch data from the local database
    private fun fetchWeatherFromLocal(city: String): Flow<WeatherResponse> {
        return flow {
            val cachedData = localDataSource.getWeatherDataByCity(city)
            if (cachedData != null) {
                emit(cachedData) // Emit the cached data if available
                Log.d("WeatherRepository", "Returning cached data for city: $city")
            } else {
                Log.e("WeatherRepository", "No local data available for city: $city")
                throw Exception("No internet and no local data available for city: $city")
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // Get 3-hour interval temperatures and process them
    override suspend fun getThreeHourForecast(
        city: String,
        apiKey: String,
        units: String,
        lang: String
    ): Flow<ForecastResponse> {
        return if (isNetworkAvailable()) {
            flow {
                remoteDataSource.getWeatherForecastOverNetwork(city, apiKey, units, lang).collect { forecastResponse ->
                    // Take the first 8 items from the forecast list
                    val nextEightForecasts = forecastResponse.list.take(8)

                    // Create a new ForecastResponse with the filtered list
                    val updatedForecastResponse = ForecastResponse(
                        list = nextEightForecasts,
                        city = forecastResponse.city
                    )

                    // Save to local database
                    localDataSource.saveForecastData(updatedForecastResponse)

                    // Emit the updated forecast response
                    emit(updatedForecastResponse)
                }
            }.catch { e ->
                Log.e("WeatherRepository", "Error fetching 3-hour forecast for city: $city", e)
                fetchThreeHourForecastFromLocal().collect { emit(it) }
            }
        } else {
            fetchThreeHourForecastFromLocal()
        }.onStart {
            Log.d("WeatherRepository", "Starting 3-hour forecast fetch for city: $city")
        }
    }
    override suspend fun getThreeHourForecast2(
        lat:Double,
        long :Double,
        apiKey: String,
        units: String,
        lang: String
    ): Flow<ForecastResponse> {
        return if (isNetworkAvailable()) {
            flow {
                remoteDataSource.getWeatherForecastOverNetwork2(lat,long, apiKey, units, lang).collect { forecastResponse ->
                    // Take the first 8 items from the forecast list
                    val nextEightForecasts = forecastResponse.list.take(8)

                    // Create a new ForecastResponse with the filtered list
                    val updatedForecastResponse = ForecastResponse(
                        list = nextEightForecasts,
                        city = forecastResponse.city
                    )

                    // Save to local database
                    localDataSource.saveForecastData(updatedForecastResponse)

                    // Emit the updated forecast response
                    emit(updatedForecastResponse)
                }
            }.catch { e ->
                Log.e("WeatherRepository", "Error fetching 3-hour forecast for geo : $lat , $long ", e)
                fetchThreeHourForecastFromLocal().collect { emit(it) }
            }
        } else {
            fetchThreeHourForecastFromLocal()
        }.onStart {
            Log.d("WeatherRepository", "Starting 3-hour forecast fetch for  geo : $lat , $long")
        }
    }
    private fun fetchThreeHourForecastFromLocal(): Flow<ForecastResponse> = flow {
        val cachedData = localDataSource.getLatestForecastData()
        if (cachedData != null) {
            val nextEightForecasts = cachedData.list.take(8)
            emit(cachedData.copy(list = nextEightForecasts))
            Log.d("WeatherRepository", "Returning cached 3-hour forecast data")
        } else {
            throw Exception("No internet and no cached 3-hour forecast data available.")
        }
    }

    // Get daily temperatures by grouping forecast into days
    override suspend fun getDailyForecast(
        city: String,
        apiKey: String,
        units: String,
        lang: String
    ): Flow<ForecastResponse> {
        return if (isNetworkAvailable()) {
            flow {
                remoteDataSource.getWeatherForecastOverNetwork(city, apiKey, units, lang).collect { forecastResponse ->
                    // Group by day and take one entry per day
                    val groupedForecasts = forecastResponse.list.groupBy { it.dt / 86400 }
                    val dailyForecasts = groupedForecasts.entries.take(5)
                        .flatMap { it.value.take(1) }

                    // Create a new ForecastResponse with daily forecasts
                    val dailyForecast = ForecastResponse(
                        list = dailyForecasts,
                        city = forecastResponse.city
                    )

                    // Save to local database
                    localDataSource.saveForecastData(dailyForecast)

                    // Emit the daily forecast
                    emit(dailyForecast)
                }
            }
        } else {
            fetchDailyForecastFromLocal()
        }.onStart {
            Log.d("WeatherRepository", "Starting daily forecast fetch for city: $city")
        }.catch { e ->
            Log.e("WeatherRepository", "Error fetching daily forecast for city: $city", e)
            throw e
        }
    }
    override suspend fun getDailyForecast2(
        lat:Double,
        long :Double,
        apiKey: String,
        units: String,
        lang: String
    ): Flow<ForecastResponse> {
        return if (isNetworkAvailable()) {
            flow {
                remoteDataSource.getWeatherForecastOverNetwork2(lat, long, apiKey, units, lang).collect { forecastResponse ->
                    // Group by day and take one entry per day
                    val groupedForecasts = forecastResponse.list.groupBy { it.dt / 86400 }
                    val dailyForecasts = groupedForecasts.entries.take(5)
                        .flatMap { it.value.take(1) }

                    // Create a new ForecastResponse with daily forecasts
                    val dailyForecast = ForecastResponse(
                        list = dailyForecasts,
                        city = forecastResponse.city
                    )

                    // Save to local database
                    localDataSource.saveForecastData(dailyForecast)

                    // Emit the daily forecast
                    emit(dailyForecast)
                }
            }
        } else {
            fetchDailyForecastFromLocal()
        }.onStart {
            Log.d("WeatherRepository", "Starting daily forecast fetch for geo : $lat , $long")
        }.catch { e ->
            Log.e("WeatherRepository", "Error fetching daily forecast for geo : $lat , $long", e)
            throw e
        }
    }

    private fun fetchDailyForecastFromLocal(): Flow<ForecastResponse> = flow {
        val cachedData = localDataSource.getLatestForecastData()
        if (cachedData != null) {
            val groupedForecasts = cachedData.list.groupBy { it.dt / 86400 }
            val dailyForecasts = groupedForecasts.entries.take(5)
                .flatMap { it.value.take(1) }
            emit(cachedData.copy(list = dailyForecasts))
            Log.d("WeatherRepository", "Returning cached daily forecast data")
        } else {
            throw Exception("No internet and no cached daily forecast data available.")
        }
    }

    // Save a city as a favorite
    override suspend fun saveFavoriteCity(city: FavoriteCity) {

        localDataSource.saveFavoriteCity(city)
    }

    // Retrieve all favorite cities
    override suspend fun getFavoriteCities(): Flow<List<FavoriteCity>> {
        return localDataSource.getFavoriteCities()
    }

    // Delete a specific favorite city by its ID
    override suspend fun deleteFavoriteCity(cityId: Int) {
        localDataSource.deleteFavoriteCity(cityId)
    }

    override suspend fun insertAlarm(alarm: Alarm) {
        localDataSource.insertAlarm(alarm)
    }

    override suspend fun getAllAlarms(): List<Alarm> {
        return localDataSource.getAllAlarms()
    }

    override suspend fun deleteAlarm(alarm: Alarm) {
        localDataSource.deleteAlarm(alarm)
    }

}