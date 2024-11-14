package com.example.weatherforecastapplication.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    private val gson = Gson()

    // Weather List Converters
    @TypeConverter
    fun fromWeatherList(weather: List<Weather>?): String = gson.toJson(weather)

    @TypeConverter
    fun toWeatherList(value: String): List<Weather> {
        val listType = object : TypeToken<List<Weather>>() {}.type
        return gson.fromJson(value, listType)
    }
    @TypeConverter
    fun fromCity(city: City?): String? {
        return gson.toJson(city)
    }

    @TypeConverter
    fun toCity(cityString: String?): City? {
        return gson.fromJson(cityString, object : TypeToken<City>() {}.type)
    }
    // Coord Converters
    @TypeConverter
    fun fromCoord(coord: Coord): String = gson.toJson(coord)

    @TypeConverter
    fun toCoord(value: String): Coord = gson.fromJson(value, Coord::class.java)

    // Main Converters
    @TypeConverter
    fun fromMain(main: Main): String = gson.toJson(main)

    @TypeConverter
    fun toMain(value: String): Main = gson.fromJson(value, Main::class.java)

    // Wind Converters
    @TypeConverter
    fun fromWind(wind: Wind): String = gson.toJson(wind)

    @TypeConverter
    fun toWind(value: String): Wind = gson.fromJson(value, Wind::class.java)

    // Clouds Converters
    @TypeConverter
    fun fromClouds(clouds: Clouds): String = gson.toJson(clouds)

    @TypeConverter
    fun toClouds(value: String): Clouds = gson.fromJson(value, Clouds::class.java)

    // Sys Converters
    @TypeConverter
    fun fromSys(sys: Sys): String = gson.toJson(sys)

    @TypeConverter
    fun toSys(value: String): Sys = gson.fromJson(value, Sys::class.java)

    // Forecast List Converters
    @TypeConverter
    fun fromForecastList(value: List<Forecast>): String = gson.toJson(value)

    @TypeConverter
    fun toForecastList(value: String): List<Forecast> {
        val listType = object : TypeToken<List<Forecast>>() {}.type
        return gson.fromJson(value, listType)
    }
}
