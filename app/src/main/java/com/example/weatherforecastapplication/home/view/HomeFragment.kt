package com.example.weatherforecastapplication.home.view

import HourlyForecastAdapter
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.databinding.FragmentHomeBinding
import com.example.weatherforecastapplication.home.viewmodel.ApiState
import com.example.weatherforecastapplication.home.viewmodel.HomeViewModel
import com.example.weatherforecastapplication.home.viewmodel.HomeViewModelFactory
import com.example.weatherforecastapplication.model.Forecast
import com.example.weatherforecastapplication.model.WeatherRepositoryImpl
import com.example.weatherforecastapplication.model.WeatherResponse
import com.example.weatherforecastapplication.network.RetrofitHelper
import com.example.weatherforecastapplication.network.WeatherRemoteDataSourceImpl
import com.example.weatherforecastapplication.network.WeatherService
import com.google.android.gms.location.*

import java.util.Locale

class HomeFragment : Fragment(R.layout.fragment_home) {

    //location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var geocoder: Geocoder
    private val REQUEST_LOCATION_CODE = 5005

    // Declare the binding variable
    private lateinit var binding: FragmentHomeBinding

    //view model
    private lateinit var viewModel: HomeViewModel
    private lateinit var viewModelFactory: HomeViewModelFactory

    private lateinit var hourlyForecastAdapter: HourlyForecastAdapter
    private lateinit var dailyForecastAdapter: DailyForecastAdapter

    private var isWeatherFetched = false

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the binding object
        binding = FragmentHomeBinding.bind(view)

        // Initialize the repository
        val weatherRepository = WeatherRepositoryImpl.getInstance(
            WeatherRemoteDataSourceImpl.getInstance(RetrofitHelper.getInstance().create(WeatherService::class.java))
        )

        // Initialize ViewModelFactory with the repository
        viewModelFactory = HomeViewModelFactory(weatherRepository)

        // Initialize ViewModel using ViewModelProvider
        viewModel = ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)

        // Initialize RecyclerViews
        initHourlyRecyclerView()
        initDailyRecyclerView()

        // Initialize UI visibility
        initUI()

        geocoder = Geocoder(requireContext(), Locale.getDefault())

        // Observe weather data
        lifecycleScope.launchWhenStarted {
            viewModel.weatherData.collect { state  ->
                when (state) {
                    is ApiState.Loading -> showLoading(true)
                    is ApiState.Success -> {
                        showLoading(false)
                        updateWeatherUI(state.weatherResponse)
                        showMainContent(true)
                    }
                    is ApiState.Failure -> {
                        showLoading(false)
                        showError(state.message)
                        showMainContent(false) // Hide main content in case of error
                    }
                }
            }
        }

        // Observe forecast data
        lifecycleScope.launchWhenStarted {
            viewModel.threeHourForecast.collect { forecastResponse ->
                forecastResponse?.let { forecasts ->
                    if (forecasts.isNotEmpty()) {
                        // Get the first 8 entries if they exist
                        val hourlyForecast: List<Forecast> = forecasts.take(8)
                        hourlyForecastAdapter.submitList(hourlyForecast)
                    } else {
                        Log.e("HomeFragment", "Forecast list is empty")
                    }
                } ?: run {
                    Log.e("HomeFragment", "Forecast response is null")
                }
            }
        }

        // Observe daily forecast data
        lifecycleScope.launchWhenStarted {
            viewModel.dailyForecast.collect { dailyForecastResponse ->
                dailyForecastResponse?.let { forecasts ->
                    dailyForecastAdapter.submitList(forecasts)
                }
            }
        }

        // Check and request location permissions
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                getFreshLocation()
            } else {
                enableLocationServices()
            }
        } else {
            requestLocationPermission()
        }
    }

    private fun initHourlyRecyclerView() {
        hourlyForecastAdapter = HourlyForecastAdapter()
        binding.hourlyForecastRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.hourlyForecastRecyclerView.adapter = hourlyForecastAdapter
    }

    private fun initDailyRecyclerView() {
        dailyForecastAdapter = DailyForecastAdapter()
        binding.dailyForecastRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.dailyForecastRecyclerView.adapter = dailyForecastAdapter
    }

    private fun updateWeatherUI(weather: WeatherResponse) {
        binding.textCityName.text = weather.name
        binding.textWeatherCondition.text = weather.weather[0].description
        binding.textCurrentTemp.text = "${weather.main.temp.toInt()} °C"

        // Update the weather icon based on the weather condition code
        val iconResId = getWeatherIconResId(weather.weather[0].id)
        binding.imageWeatherCondition.setImageResource(iconResId) // Assuming you have an ImageView for the icon

    }

    private fun getWeatherIconResId(conditionId: Int): Int {
        return when (conditionId) {
            in 200..232 -> R.drawable.storm // Thunderstorm
            in 300..321 -> R.drawable.rainy // Drizzle
            in 500..531 -> R.drawable.rainy // Rain
            in 600..622 -> R.drawable.snowy // Snow
            in 701..781 -> R.drawable.cloudy_3 // Atmosphere
            800 -> R.drawable.sun // Clear
            in 801..804 -> R.drawable.cloudy_sunny // Clouds
            else -> R.drawable.cloudy_3 // Default icon
        }
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION),
            REQUEST_LOCATION_CODE
        )
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private fun getFreshLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())


        val locationRequest = LocationRequest.Builder(0).apply {
            setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location = locationResult.lastLocation
                location?.let {
                    val cityName = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                        ?.get(0)?.locality ?: "Unknown Location"
                    fetchWeatherData(cityName)
                    isWeatherFetched = true // Set to true after the first API call

                }
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    private fun fetchWeatherData(cityName: String) {
        if(isWeatherFetched )
        {

        }
        else
        {
            viewModel.fetchWeather(cityName, "metric")
            viewModel.fetchDailyForecast(cityName, "metric")
            viewModel.fetchThreeHourForecast(cityName, "metric")
        }

    }

    private fun enableLocationServices() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }
    private fun initUI() {
        // Initially hide main content until data is loaded
        binding.mainContentLayout.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }
    private fun showMainContent(isVisible: Boolean) {
        binding.mainContentLayout.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    private fun showError(message: String) {
        binding.textError.apply {
            text = message
            visibility = View.VISIBLE
        }
    }
}
