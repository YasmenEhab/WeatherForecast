package com.example.weatherforecastapplication.home.view

import HourlyForecastAdapter
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.databinding.FragmentHomeBinding
import com.example.weatherforecastapplication.db.AppDatabase
import com.example.weatherforecastapplication.db.FavoriteCityLocalDataSourceImpl
import com.example.weatherforecastapplication.home.viewmodel.ApiState
import com.example.weatherforecastapplication.home.viewmodel.ForecastState
import com.example.weatherforecastapplication.home.viewmodel.HomeViewModel
import com.example.weatherforecastapplication.home.viewmodel.HomeViewModelFactory
import com.example.weatherforecastapplication.map.MapFragment
import com.example.weatherforecastapplication.model.FavoriteCity
import com.example.weatherforecastapplication.model.WeatherRepositoryImpl
import com.example.weatherforecastapplication.model.WeatherResponse
import com.example.weatherforecastapplication.network.RetrofitHelper
import com.example.weatherforecastapplication.network.WeatherRemoteDataSourceImpl
import com.example.weatherforecastapplication.network.WeatherService
import com.example.weatherforecastapplication.map.SharedViewModel
import com.google.android.gms.location.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import androidx.lifecycle.Observer


import java.util.Locale

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val sharedViewModel: SharedViewModel by activityViewModels()


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
    private var currentCityName: String? = null

    // Shared Preferences
    private lateinit var sharedPreferences: SharedPreferences
    private var languageOption: String = "en"
    private var unitOption: String = "metric"
    private var locationOption: String = "gps"
    var locationFlag = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Observe the selected city using the correct Observer
        sharedViewModel.selectedCity.observe(viewLifecycleOwner, Observer { cityName ->
            // Update UI with the selected city
            // For example, you can display the city name in a TextView
            Log.d("HomeFragment", "Received selected city: $cityName") // Log the received city
            binding.textCityName.text = cityName // Update your TextView here
            fetchWeatherData(cityName)
            locationFlag = true


        })

        return view
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the binding object
        binding = FragmentHomeBinding.bind(view)

        // Initialize UI visibility
        initUI()

        // Obtain an instance of FavoriteCityDao
        val favoriteCityDao = AppDatabase.getDatabase(requireContext()).favoriteCityDao()

        // Create an instance of the local data source
        val favoriteCityLocalDataSource = FavoriteCityLocalDataSourceImpl(favoriteCityDao)

        // Initialize the repository
        val weatherRepository = WeatherRepositoryImpl.getInstance(
            WeatherRemoteDataSourceImpl.getInstance(
                RetrofitHelper.getInstance().create(WeatherService::class.java)
            ),localDataSource = favoriteCityLocalDataSource
        )


        // Initialize SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("Settings", Context.MODE_PRIVATE)
        loadUserPreferences()
        Log.d("HomeFragment", "Saved location option: $locationOption")


        // Set the locale based on saved preference
        setLocale()

        // Initialize ViewModelFactory with the repository
        viewModelFactory = HomeViewModelFactory(weatherRepository, sharedPreferences)

        // Initialize ViewModel using ViewModelProvider
        viewModel = ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)

        // Initialize RecyclerViews
        initHourlyRecyclerView()
        initDailyRecyclerView()

        geocoder = Geocoder(requireContext(), if (languageOption == "ar") Locale("ar") else Locale("en"))


        //on map clicked listener
        binding.locationimg.setOnClickListener {
            Log.d("HomeFragment", "Navigating to MapFragment") // Log the navigation

            locationFlag =true
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, MapFragment())
                .addToBackStack(null) // Add to back stack to allow navigation back
                .commit()
        }

        // Check location option and retrieve location based on preference

            if (checkPermissions()) {
                if (isLocationEnabled()) {
                    Log.d("HomeFragment", "getFreshLocation ")
                    getFreshLocation()
                } else {
                    enableLocationServices()
                }
            } else {
                requestLocationPermission()
            }



        // Observe weather data
        lifecycleScope.launchWhenStarted {
            viewModel.weatherData.collect { state ->
                when (state) {
                    is ApiState.Loading ->{showLoading(true)
                        Log.e("HomeFragment", "weatherData loading")
                    }

                    is ApiState.Success -> {
                        showLoading(false)
                        updateWeatherUI(state.weatherResponse)
                        showMainContent(true)
                        Log.e("HomeFragment", "weatherData has been fetched")
                    }

                    is ApiState.Failure -> {
                        showLoading(false)
                        showError(state.message)
                        showMainContent(false) // Hide main content in case of error
                        Log.e("HomeFragment", "weatherData has not been fetched")
                    }
                }
            }
        }

        // Observe hourly forecast data
        lifecycleScope.launchWhenStarted {
            viewModel.threeHourForecast.collect { state ->
                when (state) {
                    is ForecastState.Loading -> {
                        showLoading(true)
                        Log.e("HomeFragment", "Hourly Forecast loading")
                    }
                    is ForecastState.Success -> {
                        showLoading(false)
                        // Assuming state.weatherResponse.list contains the list of forecasts
                        hourlyForecastAdapter.submitList(state.forecastResponse.list)
                        showMainContent(true)
                        Log.e("HomeFragment", "Hourly Forecast data has been fetched")
                    }
                    is ForecastState.Failure -> {
                        showLoading(false)
                        showError(state.message) // Display error message
                        Log.e("HomeFragment", "Failed to fetch hourly forecast: ${state.message}")
                    }
                }
            }
        }

        // Observe daily forecast data
        lifecycleScope.launchWhenStarted {
            viewModel.dailyForecast.collect { state ->
                when (state) {
                    is ForecastState.Loading -> {
                        showLoading(true)
                        Log.e("HomeFragment", "Daily Forecast loading")
                    }

                    is ForecastState.Success -> {
                        showLoading(false)
                        // Assuming state.weatherResponse.list contains the list of forecasts
                        dailyForecastAdapter.submitList(state.forecastResponse.list)
                        Log.e("HomeFragment", "Daily Forecast data has been fetched")
                    }

                    is ForecastState.Failure -> {
                        showLoading(false)
                        showError(state.message) // Display error message
                        Log.e("HomeFragment", "Failed to fetch daily forecast: ${state.message}")
                    }
                }
            }
        }


    }

    private fun setLocale() {
        val languageOption = sharedPreferences.getString("LANGUAGE", "en") ?: "en"
        val locale = if (languageOption == "ar") {
            Locale("ar")
        } else {
            Locale("en")
        }

        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun initHourlyRecyclerView() {
        hourlyForecastAdapter = HourlyForecastAdapter()
        binding.hourlyForecastRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.hourlyForecastRecyclerView.adapter = hourlyForecastAdapter
    }

    private fun initDailyRecyclerView() {
        dailyForecastAdapter = DailyForecastAdapter(languageOption)
        binding.dailyForecastRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.dailyForecastRecyclerView.adapter = dailyForecastAdapter
    }

    private fun displayCurrentDate() {
        val locale = if (languageOption == "ar") {
            Locale("ar") // Arabic
        } else {
            Locale("en") // Default to English
        }
        // Get the current date
        val currentDate = SimpleDateFormat("EEEE, d MMMM yyyy", locale).format(Date())

        // Set the current date to the TextView
        binding.textDate.text = currentDate
    }

    private fun getUnits(): Pair<String, String> {
        val windUnit: String
        val temperatureUnit: String
        if(unitOption == "metric")
        {
            windUnit = if (languageOption == "ar") "م/ث" else "m/s"
            temperatureUnit = if (languageOption == "ar") "°س" else "°C"
        }
        else {
            windUnit = if (languageOption == "ar") "ميل/س" else "mph" // Default to imperial for wind
            temperatureUnit = if (unitOption == "imperial") {
                if (languageOption == "ar") "°ف" else "°F"
            } else {
                if (languageOption == "ar") "ك" else "K"
            }
        }
        return windUnit to temperatureUnit

    }

    private fun loadUserPreferences() {
        // Retrieve the language preference
        languageOption = sharedPreferences.getString("LANGUAGE", "en") ?: "en"
        unitOption = sharedPreferences.getString("TEMPERATURE_UNIT", "metric") ?: "metric"
        locationOption = sharedPreferences.getString("LOCATION_OPTION","gps") ?: "gps"

    }

     fun fetchWeatherData(cityName: String) {
        //if (!isWeatherFetched) {
            Log.d("HomeFragment", "Fetching weather with city: $cityName, unit: $unitOption, language: $languageOption")

            viewModel.fetchWeather(cityName, unitOption, languageOption)
            viewModel.fetchDailyForecast(cityName, unitOption, languageOption)
            viewModel.fetchThreeHourForecast(cityName, unitOption, languageOption)
        //}

    }
    private fun updateWeatherUI(weather: WeatherResponse) {
        binding.textCityName.text = weather.name
        binding.textWeatherCondition.text = weather.weather[0].description
        val (windUnit, temperatureUnit) = getUnits()
        binding.textCurrentTemp.text = getString(R.string.temperature_format, weather.main.temp.toInt(), temperatureUnit)
        binding.Windtext.text = getString(R.string.wind_speed_format, weather.wind.speed, windUnit)
        binding.PressureValue.text = getString(R.string.pressure_format, weather.main.pressure)
        binding.humidityUnit.text = getString(R.string.humidity_format, weather.main.humidity)
        displayCurrentDate()
        val iconResId = getWeatherIconResId(weather.weather[0].id)
        binding.imageWeatherCondition.setImageResource(iconResId)
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
        return ContextCompat.checkSelfPermission(
            requireContext(),
            ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION),
            REQUEST_LOCATION_CODE
        )
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    @SuppressLint("MissingPermission")
    private fun getFreshLocation() {

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        // Set Geocoder locale based on language option
        val geocoderLocale = if (languageOption == "ar") Locale("ar") else Locale.getDefault()
        geocoder = Geocoder(requireContext(), geocoderLocale)  // Set the locale for Geocoder


        val locationRequest = LocationRequest.Builder(0).apply {
            setPriority(Priority.PRIORITY_HIGH_ACCURACY)

        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location = locationResult.lastLocation
                location?.let {
                    val newCityName = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                        ?.get(0)?.locality ?: "Unknown Location"
                    if (newCityName != currentCityName) {
                        currentCityName = newCityName
                        if(!locationFlag){
                            if (newCityName != null) {
                                fetchWeatherData(newCityName)
                            } else {
                                showError("Unable to retrieve city name from location.")
                            }
                            Log.d("HomeFragment", "City changed, fetching new weather data for $newCityName")

                        }
                         }}
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
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
        binding.errorLayout.visibility = View.VISIBLE
        binding.buttonRetry.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                retryFetchingWeatherData()  // Call a retry method
            }
        }
        showMainContent(false) // Hide main content in case of error
    }

    private fun retryFetchingWeatherData() {
        binding.errorLayout.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE  // Show loading

        // Attempt to refetch location and weather data
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
    override fun onDestroy() {
        super.onDestroy()
        // Clean up location updates
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }


}