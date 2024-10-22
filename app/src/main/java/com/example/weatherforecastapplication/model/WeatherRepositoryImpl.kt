package com.example.weatherforecastapplication.model


import com.example.weatherforecastapplication.network.WeatherRemoteDataSource
import kotlinx.coroutines.flow.Flow

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

    override suspend fun getProducts(city: String, apiKey: String, units: String): Flow<WeatherResponse> {

            return remoteDataSource.getWeatherInfoOverNetwork(city , apiKey ,units )

    }

}