package com.example.weatherforecastapplication.db

import com.example.weatherforecastapplication.model.FavoriteCity
import kotlinx.coroutines.flow.Flow


interface FavoriteCityLocalDataSource {
    suspend fun saveFavoriteCity(city: FavoriteCity)
    suspend fun getFavoriteCities(): Flow<List<FavoriteCity>>
    suspend fun deleteFavoriteCity(cityId: Int)
    suspend fun addFavoriteCity(cityName: String)
}