package com.example.weatherforecastapplication.fav.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherforecastapplication.FragmentNavigation
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.databinding.FragmentFavBinding
import com.example.weatherforecastapplication.db.AppDatabase
import com.example.weatherforecastapplication.db.LocalDataSourceImpl
import com.example.weatherforecastapplication.fav.viewmodel.FavState
import com.example.weatherforecastapplication.fav.viewmodel.FavViewModel
import com.example.weatherforecastapplication.fav.viewmodel.FavViewModelFactory
import com.example.weatherforecastapplication.map.SharedViewModel
import com.example.weatherforecastapplication.model.FavoriteCity
import com.example.weatherforecastapplication.model.WeatherRepositoryImpl
import com.example.weatherforecastapplication.network.RetrofitHelper
import com.example.weatherforecastapplication.network.WeatherRemoteDataSourceImpl
import com.example.weatherforecastapplication.network.WeatherService
import kotlinx.coroutines.launch


class FavFragment : Fragment() , OnCityClickListener {
    lateinit var recyclerView: RecyclerView
    lateinit var FavAdapter: FavAdapter
    lateinit var mLayoutManager: LinearLayoutManager
    lateinit var viewModel: FavViewModel
    lateinit var products: List<FavoriteCity>
    lateinit var viewModelFactory: FavViewModelFactory
    private lateinit var sharedPreferences: SharedPreferences
    private var languageOption: String = "en"
    private var unitOption: String = "metric"
    private var locationOption: String = "gps"
    private lateinit var binding: FragmentFavBinding
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var fragmentNavigation: FragmentNavigation // Declare the interface



    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentNavigation = context as FragmentNavigation // Initialize the interface
    }





    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_fav, container, false)

        return view    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFavBinding.bind(view)
        recyclerView = view.findViewById(R.id.favouritesRecyclerView)
        val favoriteCityDao = AppDatabase.getDatabase(requireContext()).favoriteCityDao()
        val weatherDao = AppDatabase.getDatabase(requireContext()).weatherDao()
        val forecastDao = AppDatabase.getDatabase(requireContext()).forecastResponseDao()

        val favoriteCityLocalDataSource = LocalDataSourceImpl(favoriteCityDao,weatherDao,forecastDao)

        val weatherRepository = WeatherRepositoryImpl.getInstance(
            WeatherRemoteDataSourceImpl.getInstance(
                RetrofitHelper.getInstance().create(WeatherService::class.java)
            ),localDataSource = favoriteCityLocalDataSource,
            requireContext()
        )

        // Initialize SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("Settings", Context.MODE_PRIVATE)
        loadUserPreferences()
        viewModelFactory = FavViewModelFactory(weatherRepository, sharedPreferences)
        viewModel = ViewModelProvider(this, viewModelFactory).get(FavViewModel::class.java)
        viewModel.getAllFavCities()
        initFavRecyclerView()

        lifecycleScope.launch {  viewModel.weatherData.collect{ state ->
            when (state) {
                is FavState.Loading ->{
                    Log.d("FavFragment", "cities loading")

                }
                is FavState.Success -> {
                    Log.d("FavFragment", "cities has  been fetched")

                    FavAdapter.submitList(state.weatherResponse)
                }
                is FavState.Failure -> {
                    Log.e("FavFragment", "cities has not been fetched")

                }
            }

        }}

    }
    private fun setupSearchAndAddFunctionality() {
//        binding.addToFavoritesButton.setOnClickListener {
//            val cityName = binding.searchCityAutoComplete.text.toString().trim()
//            if (cityName.isNotEmpty()) {
//                // Using Geocoder to find coordinates for the city
//                val geocoder = Geocoder(requireContext())
//                val addressList = geocoder.getFromLocationName(cityName, 1)
//                if (addressList.isNotEmpty()) {
//                    val location = addressList[0]
//                    val newFavoriteCity = FavoriteCity(cityName, location.latitude, location.longitude)
//                    addCityToFavorites(newFavoriteCity)
//                } else {
//                    Toast.makeText(requireContext(), "City not found", Toast.LENGTH_SHORT).show()
//                }
//            } else {
//                Toast.makeText(requireContext(), "Please enter a city name", Toast.LENGTH_SHORT).show()
//            }
//        }
    }
    private fun addCityToFavorites(city: FavoriteCity) {
        viewModel.addFavCity(city)
        Toast.makeText(requireContext(), "${city.cityName} added to favorites", Toast.LENGTH_SHORT).show()
    }

    private fun loadUserPreferences() {
        // Retrieve the language preference
        languageOption = sharedPreferences.getString("LANGUAGE", "en") ?: "en"
        unitOption = sharedPreferences.getString("TEMPERATURE_UNIT", "metric") ?: "metric"
        locationOption = sharedPreferences.getString("LOCATION_OPTION","gps") ?: "gps"

    }
    private fun initFavRecyclerView() {
        FavAdapter = FavAdapter(this)
        binding.favouritesRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.favouritesRecyclerView.adapter = FavAdapter
    }


    override fun onCityClick(city: FavoriteCity) {
        sharedViewModel.selectCoordinates(city.latitude , city.longitude)
        fragmentNavigation.navigateToHome(city.latitude , city.longitude) // Navigate to HomeFragment with selected city



    }

    override fun onDeleteCityClick(city: FavoriteCity) {
        viewModel.deleteFavCity(city)
    }


}