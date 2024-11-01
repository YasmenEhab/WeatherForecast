package com.example.weatherforecastapplication.db

import android.util.Log
import com.example.weatherforecastapplication.model.FavoriteCity
import kotlinx.coroutines.flow.Flow

class FavoriteCityLocalDataSourceImpl (private val favoriteCityDao: FavoriteCityDao) :FavoriteCityLocalDataSource{
    private val TAG = "FavoriteCityLocalDataSourceImpl"

    // Insert a favorite city into the database
   override suspend fun saveFavoriteCity(city: FavoriteCity) {
        favoriteCityDao.insertFavoriteCity(city)
    }

    // Retrieve all favorite cities from the database
    override suspend fun getFavoriteCities(): Flow<List<FavoriteCity>> {
        return favoriteCityDao.getAllFavoriteCities()
    }

    // Remove a specific favorite city by its ID
    override  suspend fun deleteFavoriteCity(cityId: Int) {
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
        }    }
}