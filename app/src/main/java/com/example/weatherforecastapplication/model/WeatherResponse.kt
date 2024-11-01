package com.example.weatherforecastapplication.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Main weather response data class

data class WeatherResponse(
    val coord: Coord,
    val weather: List<Weather>,
    val main: Main,
    val wind: Wind,
    val clouds: Clouds,
    val name: String,
    val sys: Sys
)

// Coordinates of the location
data class Coord(
    val lon: Double,
    val lat: Double
)

// Weather details (description, icon, etc.)
data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

// Main weather parameters (temperature, pressure, humidity, etc.)
data class Main(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Int,
    val humidity: Int
)

// Wind details (speed, degree)
data class Wind(
    val speed: Double,
    val deg: Int
)

// Cloudiness percentage
data class Clouds(
    val all: Int
)

// System data (country, etc.)
data class Sys(
    val country: String
)

// Forecast response data class
data class ForecastResponse(
    val list: List<Forecast>,
    val city: City
)

// Forecast data class for each 3-hour interval
data class Forecast(
    val dt: Long,
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind,
    val clouds: Clouds
)

// Main weather parameters specific for forecast
data class MainForecast(
    val temp: Double,
    val pressure: Int,
    val humidity: Int
)

// City data class to hold city-related information from forecast response
data class City(
    val name: String,
    val country: String
)

@Entity(tableName = "favorite_city_table")
data class FavoriteCity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cityName: String,
    val country: String="",
    val latitude: Double=0.0,
    val longitude: Double=0.0
)
