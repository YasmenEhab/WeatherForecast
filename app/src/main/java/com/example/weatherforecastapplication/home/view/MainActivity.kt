package com.example.weatherforecastapplication.home.view

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.home.viewmodel.HomeViewModel
import com.example.weatherforecastapplication.home.viewmodel.HomeViewModelFactory
import com.example.weatherforecastapplication.model.Forecast
import com.example.weatherforecastapplication.model.WeatherRepositoryImpl
import com.example.weatherforecastapplication.model.WeatherResponse
import com.example.weatherforecastapplication.network.RetrofitHelper
import com.example.weatherforecastapplication.network.WeatherRemoteDataSourceImpl
import com.example.weatherforecastapplication.network.WeatherService

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var viewModelFactory: HomeViewModelFactory
    private lateinit var hourlyForecastAdapter: HourlyForecastAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the repository
        val weatherRepository = WeatherRepositoryImpl.getInstance(
            WeatherRemoteDataSourceImpl.getInstance(RetrofitHelper.getInstance().create(WeatherService::class.java)))


        // Initialize ViewModelFactory with the repository
        viewModelFactory = HomeViewModelFactory(weatherRepository)

        // Initialize ViewModel using ViewModelProvider
        viewModel = ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)

        // Initialize RecyclerView for hourly forecast
        initHourlyRecyclerView()

        // Fetch weather for a city (you can change the city and units as required)
        viewModel.fetchWeather("London", "metric")
        viewModel.fetchDailyForecast("London", "metric")
        viewModel.fetchThreeHourForecast("London", "metric")

        // Observe weather data
        lifecycleScope.launchWhenStarted {
            viewModel.weatherData.collect { weatherResponse ->
                weatherResponse?.let {
                    // Update UI with weather data, e.g., TextViews, ImageView for icon, etc.
                    updateWeatherUI(weatherResponse)

                }
            }
        }

        // Observe forecast data
        lifecycleScope.launchWhenStarted {
            viewModel.threeHourForecast.collect { forecastResponse ->
                forecastResponse?.let { forecasts ->
                    // Check if the list is not empty and has enough entries
                    if (forecasts.isNotEmpty()) {
                        // Get the first 8 entries if they exist
                        val hourlyForecast: List<Forecast> = forecasts.take(8)
                        hourlyForecastAdapter.submitList(hourlyForecast)
                    } else {
                        // Handle the case where the list is empty
                        Log.e("MainActivity", "Forecast list is empty")
                    }
                } ?: run {
                    // Handle the case where forecastResponse is null
                    Log.e("MainActivity", "Forecast response is null")
                }
            }
        }
    }

    // Function to update the UI with weather data
    private fun updateWeatherUI(weather: WeatherResponse) {
        // Example UI updates (you should map your actual TextView IDs)
        findViewById<TextView>(R.id.text_city_name).text = weather.name
        findViewById<TextView>(R.id.text_weather_condition).text = weather.weather[0].description
        findViewById<TextView>(R.id.text_current_temp).text = "${weather.main.temp} °C"
       // findViewById<TextView>(R.id.tvHumidity).text = "${weather.main.humidity} %"
        //findViewById<TextView>(R.id.tvWindSpeed).text = "${weather.wind.speed} m/s"
       // findViewById<TextView>(R.id.tvWeatherDescription).text = weather.weather[0].description
        // etc. for other fields like pressure, clouds, etc.

    }

    private fun initHourlyRecyclerView() {
        hourlyForecastAdapter = HourlyForecastAdapter()
        val hourlyRecyclerView: RecyclerView = findViewById(R.id.hourlyForecastRecyclerView)
        hourlyRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        hourlyRecyclerView.adapter = hourlyForecastAdapter
    }
}