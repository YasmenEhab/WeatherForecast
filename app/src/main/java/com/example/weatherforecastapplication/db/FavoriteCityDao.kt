package com.example.weatherforecastapplication.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherforecastapplication.model.FavoriteCity
import com.example.weatherforecastapplication.model.WeatherResponse
import kotlinx.coroutines.flow.Flow


@Dao
interface FavoriteCityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteCity(favoriteCity: FavoriteCity)

    @Query("SELECT * FROM favorite_city_table")
    fun getAllFavoriteCities(): Flow<List<FavoriteCity>>

    @Query("DELETE FROM favorite_city_table WHERE id = :cityId")
    suspend fun deleteFavoriteCity(cityId: Int)

    @Query("SELECT * FROM favorite_city_table WHERE ROUND(longitude, 4) = ROUND(:longitude, 4) AND ROUND(latitude, 4) = ROUND(:latitude, 4) LIMIT 1")
    suspend fun getFavoriteCityByCoordinates(latitude: Double, longitude: Double): FavoriteCity?
}