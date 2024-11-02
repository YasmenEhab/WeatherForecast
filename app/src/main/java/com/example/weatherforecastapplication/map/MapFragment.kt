package com.example.weatherforecastapplication.map

import android.app.AlertDialog
import android.content.Context
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
import com.example.weatherforecastapplication.db.FavoriteCityLocalDataSourceImpl
import com.example.weatherforecastapplication.home.view.HomeFragment
import com.example.weatherforecastapplication.model.FavoriteCity
import com.example.weatherforecastapplication.model.WeatherRepository
import com.example.weatherforecastapplication.model.WeatherRepositoryImpl
import com.example.weatherforecastapplication.network.RetrofitHelper
import com.example.weatherforecastapplication.network.WeatherRemoteDataSourceImpl
import com.example.weatherforecastapplication.network.WeatherService
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
    private lateinit var osmMapView: MapView
    private val TAG = "MapFragment"

private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        // Initialize the OsmDroid configuration
        Configuration.getInstance().userAgentValue = requireContext().packageName

        // Set up the MapView
        osmMapView = view.findViewById(R.id.osmMapView)
        osmMapView.setTileSource(TileSourceFactory.MAPNIK)
        osmMapView.setMultiTouchControls(true)

        // Set initial map zoom and location
        val mapController: IMapController = osmMapView.controller
        mapController.setZoom(5.0)
        mapController.setCenter(GeoPoint(20.0, 78.0)) // Center on a default location

        // Add MapEventsOverlay to handle tap events
        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                // Clear existing markers
                osmMapView.overlays.clear()

                // Add a new marker at the selected location
                val marker = Marker(osmMapView).apply {
                    position = p
                    title = "Selected Location"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    infoWindow = BasicInfoWindow(R.layout.custom_info_window, osmMapView)
                }
                osmMapView.overlays.add(marker)
                osmMapView.invalidate()

                // Use Geocoder to get the city name
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addressList = geocoder.getFromLocation(p.latitude, p.longitude, 1)
                val cityName = addressList?.firstOrNull()?.locality ?: "Unknown Location"
                Log.d(TAG, "City name retrieved: $cityName")

                // Show dialog to ask user for next action
                showLocationOptionsDialog(cityName, p)

                return true
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                return false
            }
        }

        // Add the overlay to the map
        val eventsOverlay = MapEventsOverlay(mapEventsReceiver)
        osmMapView.overlays.add(eventsOverlay)

        return view
    }

    // Function to show options dialog
    private fun showLocationOptionsDialog(cityName: String, geoPoint: GeoPoint) {
        AlertDialog.Builder(requireContext())
            .setTitle("Location Selected: $cityName")
            .setMessage("Would you like to view detailed weather or add this location to favorites?")
            .setPositiveButton("View Weather") { _, _ ->
                // Call function to view weather details for the selected location
                viewWeatherDetails(geoPoint, cityName)
            }
            .setNegativeButton("Add to Favorites") { _, _ ->
                // Call function to add location to favorites
                addToFavorites(cityName, geoPoint)
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    // Function to view detailed weather
    private fun viewWeatherDetails(geoPoint: GeoPoint, cityName: String) {
        Log.d(TAG, "Viewing weather for location: City = $cityName, Latitude = ${geoPoint.latitude}, Longitude = ${geoPoint.longitude}")

        // Use the shared ViewModel to pass the selected city
        sharedViewModel.selectCity(cityName)

        // Navigate to HomeFragment
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.nav_host_fragment, HomeFragment())
            addToBackStack(null)
            commit()
        }
    }
    // Assume this method gets called when a city is selected
    private fun onCitySelected(cityName: String) {
        Log.d("MapFragment", "Selected city: $cityName") // Log the selected city
        sharedViewModel.selectCity(cityName)
    }

    // Function to add location to favorites
    private fun addToFavorites(cityName: String, geoPoint: GeoPoint) {
        Log.d(TAG, "Added to favorites: $cityName at Latitude = ${geoPoint.latitude}, Longitude = ${geoPoint.longitude}")
        // Save to favorites in your app's data store (e.g., Shared Preferences, database)
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

        // Launch a coroutine to save the favorite city
        lifecycleScope.launch {
            try {
                val favoriteCity = FavoriteCity(cityName = cityName)
                weatherRepository.saveFavoriteCity(favoriteCity)
                Toast.makeText(context, "$cityName added to favorites!", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "City $cityName successfully added to favorites in the database.")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding city to favorites: ${e.message}")
                Toast.makeText(context, "Failed to add $cityName to favorites.", Toast.LENGTH_SHORT).show()
            }
        }
    }



    override fun onResume() {
        super.onResume()
        osmMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        osmMapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        osmMapView.onDetach() // Important for cleaning up resources
    }
}
