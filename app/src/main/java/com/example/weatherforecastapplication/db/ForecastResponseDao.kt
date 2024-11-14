package com.example.weatherforecastapplication.db
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherforecastapplication.model.ForecastResponse

@Dao
interface ForecastResponseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecastResponse(forecastResponse: ForecastResponse)

    @Query("SELECT * FROM forecast_response_table ORDER BY id DESC LIMIT 1")
    suspend fun getLatestForecastResponse(): ForecastResponse?

    // Optional: Retrieve all forecast responses if needed for further processing or debugging
    @Query("SELECT * FROM forecast_response_table")
    suspend fun getAllForecastResponses(): List<ForecastResponse>
}