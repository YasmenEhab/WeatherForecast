package com.example.weatherforecastapplication.model

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
