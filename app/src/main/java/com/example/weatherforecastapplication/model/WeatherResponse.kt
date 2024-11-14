package com.example.weatherforecastapplication.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName

// Main weather response data class
@Entity(tableName = "weather_table")
@TypeConverters(Converters::class)
data class WeatherResponse(
    @PrimaryKey(autoGenerate = true)
    val id : Int= 0,
    @Embedded(prefix = "coord_")
    val coord: Coord,
    val weather: List<Weather> = emptyList(),
    val main: Main= Main(),
    val wind: Wind= Wind(),
    val clouds: Clouds= Clouds(),
    val name: String = "",
    val sys: Sys= Sys()
)

// Coordinates of the location
data class Coord(
    var lon: Double = 0.0,
    var lat: Double = 0.0
)

// Weather details (description, icon, etc.)
data class Weather(
    val id: Int= 0,
    val main: String = "",
    val description: String = "",
    val icon: String = ""
)

// Main weather parameters (temperature, pressure, humidity, etc.)
data class Main(
    val temp: Double= 0.0,
    val feels_like: Double= 0.0,
    val temp_min: Double= 0.0,
    val temp_max: Double= 0.0,
    val pressure: Int= 0,
    val humidity: Int= 0
)

// Wind details (speed, degree)
data class Wind(
    val speed: Double= 0.0,
    val deg: Int= 0
)

// Cloudiness percentage
data class Clouds(
    val all: Int= 0
)

// System data (country, etc.)
data class Sys(
    val country: String= ""
)


@Entity(tableName = "forecast_response_table")
@TypeConverters(Converters::class)
// Forecast response data class
data class ForecastResponse(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val list: List<Forecast>,
    val city: City
)

// Forecast data class for each 3-hour interval
data class Forecast(
    val dt: Long,
    @Embedded
    val main: MainForecast,
    val weather: List<Weather>,
    @Embedded
    val wind: Wind,
    @Embedded
    val clouds: Clouds
)

// Main weather parameters specific for forecast
data class MainForecast(
    val temp: Double=0.0,
    val temp_min: Double=0.0,
    val temp_max: Double=0.0,
    val pressure: Int=0,
    val humidity: Int=0
)

// City data class to hold city-related information from forecast response
data class City(

    val name: String,
    @Embedded(prefix = "coord_")
    val coord: Coord,
    val country: String="",
    val population: Int=0,
    val timezone: Int=0,
    val sunrise: Long=0,
    val sunset: Long=0
)

@Entity(tableName = "favorite_city_table")
data class FavoriteCity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cityName: String,
    val country: String="",
    var latitude: Double=0.0,
    var longitude: Double=0.0
)
