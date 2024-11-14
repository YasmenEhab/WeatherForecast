package com.example.weatherforecastapplication.map

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.db.AppDatabase
import com.example.weatherforecastapplication.db.LocalDataSourceImpl
import com.example.weatherforecastapplication.home.view.HomeFragment
import com.example.weatherforecastapplication.model.FavoriteCity
import com.example.weatherforecastapplication.model.WeatherRepositoryImpl
import com.example.weatherforecastapplication.network.RetrofitHelper
import com.example.weatherforecastapplication.network.WeatherRemoteDataSourceImpl
import com.example.weatherforecastapplication.network.WeatherService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow
import java.util.Locale

class MapFragment : Fragment() {
    private lateinit var map: MapView
    private lateinit var mapController: IMapController
    private val viewModel: SharedViewModel by activityViewModels()
    private val geocoder by lazy { Geocoder(requireContext(), Locale.getDefault()) }
    private val TAG = "MapFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        Configuration.getInstance().userAgentValue = requireContext().packageName
        map = view.findViewById(R.id.osmMapView)
        initializeMap()
        return view
    }

    private fun initializeMap() {
        Configuration.getInstance().load(
            requireContext(),
            requireContext().getSharedPreferences("map_prefs", Context.MODE_PRIVATE)
        )
        map.setTileSource(TileSourceFactory.MAPNIK)
        mapController = map.controller
        mapController.setZoom(10.0)
        mapController.setCenter(GeoPoint(30.033333, 31.233334)) // Default center (Cairo, Egypt)

        // Add map event overlays for user interaction
        val overlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                p?.let { showLocationOptionsDialog(it) }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        })
        map.overlays.add(overlay)
    }

    private fun showLocationOptionsDialog(location: GeoPoint) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Get the location name using Geocoder
                val addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                val address = addressList?.firstOrNull()?.getAddressLine(0) ?: "Unknown location"

                launch(Dispatchers.Main) {
                    // Create and display the dialog
                    AlertDialog.Builder(requireContext())
                        .setTitle("Select Location")
                        .setMessage("You selected: $address.\nWhat would you like to do?")
                        .setPositiveButton("View Weather Details") { _, _ ->
                            viewWeatherDetails(location)
                        }
                        .setNegativeButton("Add to Favorites") { _, _ ->
                            addToFavorites(location, address)
                        }
                        .setNeutralButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            } catch (e: Exception) {
                Log.e("MapFragment", "Geocoding error: ${e.message}")
                launch(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Error retrieving location information",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun viewWeatherDetails(location: GeoPoint) {
        Toast.makeText(
            requireContext(),
            "Navigating to weather details for: ${location.latitude}, ${location.longitude}",
            Toast.LENGTH_SHORT
        ).show()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Get the city name using Geocoder
                val addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                val cityName = addressList?.firstOrNull()?.locality ?: "Unknown Location"

                // Set the city name in the shared ViewModel
                launch(Dispatchers.Main) {
                   // viewModel.selectCity(cityName)
                    viewModel.selectCoordinates(location.latitude, location.longitude)


                    // Navigate to HomeFragment
                    parentFragmentManager.beginTransaction().apply {
                        replace(R.id.nav_host_fragment, HomeFragment())
                        addToBackStack(null)
                        commit()
                    }
                }
            } catch (e: Exception) {
                Log.e("MapFragment", "Geocoding error: ${e.message}")
                launch(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error retrieving city name", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun addToFavorites(location: GeoPoint, locationName: String) {
        Log.d(TAG, "Added to favorites: $locationName at Latitude = ${location.latitude}, Longitude = ${location.longitude}")

        lifecycleScope.launch(Dispatchers.IO) {
//            val favoriteCity = FavoriteCity(locationName, location.latitude, location.longitude)
//            AppDatabase.getDatabase(requireContext()).favoriteCityDao().insert(favoriteCity)

            val favoriteCityDao = AppDatabase.getDatabase(requireContext()).favoriteCityDao()
            val weatherDao = AppDatabase.getDatabase(requireContext()).weatherDao()
            val forecastDao = AppDatabase.getDatabase(requireContext()).forecastResponseDao()

            // Create an instance of the local data source
            val favoriteCityLocalDataSource = LocalDataSourceImpl(favoriteCityDao,weatherDao,forecastDao)
            val remoteDataSource = WeatherRemoteDataSourceImpl.getInstance(
                RetrofitHelper.getInstance().create(WeatherService::class.java)
            )
            // Initialize the repository
            val weatherRepository = WeatherRepositoryImpl.getInstance(remoteDataSource, favoriteCityLocalDataSource,requireContext())

            launch(Dispatchers.Main) {
                Toast.makeText(
                    requireContext(),
                    "$locationName added to favorites",
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.selectCoordinates(location.latitude, location.longitude)
                val favoriteCity = FavoriteCity(
                    cityName = locationName,
                    latitude = location.latitude,
                    longitude = location.longitude
                )
                weatherRepository.saveFavoriteCity(favoriteCity)
                addMarker(
                    location,
                    locationName
                ) // Optionally, add a marker to indicate favorite location
            }
        }
    }

    private fun addMarker(location: GeoPoint, locationName: String) {
        val drawable = resources.getDrawable(R.drawable.img_2, null)
        val bitmap = (drawable as BitmapDrawable).bitmap
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 50, 50, false)

        val marker = Marker(map).apply {
            position = location
            title = locationName
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = BitmapDrawable(resources, scaledBitmap)
            infoWindow = BasicInfoWindow(R.layout.custom_info_window, map)
            setOnMarkerClickListener { _, _ ->
                Toast.makeText(requireContext(), "Marker at: $title", Toast.LENGTH_SHORT).show()
                true
            }
        }
        map.overlays.add(marker)
        map.invalidate() // Refresh the map to show the new marker
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        map.onDetach() // Clean up resources
    }
}
