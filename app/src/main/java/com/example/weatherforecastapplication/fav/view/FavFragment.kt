package com.example.weatherforecastapplication.fav.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherforecastapplication.FragmentNavigation
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.databinding.FragmentFavBinding
import com.example.weatherforecastapplication.databinding.FragmentHomeBinding
import com.example.weatherforecastapplication.db.AppDatabase
import com.example.weatherforecastapplication.db.FavoriteCityDao
import com.example.weatherforecastapplication.db.FavoriteCityLocalDataSource
import com.example.weatherforecastapplication.db.FavoriteCityLocalDataSourceImpl
import com.example.weatherforecastapplication.fav.viewmodel.FavState
import com.example.weatherforecastapplication.fav.viewmodel.FavViewModel
import com.example.weatherforecastapplication.fav.viewmodel.FavViewModelFactory
import com.example.weatherforecastapplication.home.viewmodel.ApiState
import com.example.weatherforecastapplication.home.viewmodel.HomeViewModel
import com.example.weatherforecastapplication.home.viewmodel.HomeViewModelFactory
import com.example.weatherforecastapplication.map.SharedViewModel
import com.example.weatherforecastapplication.model.FavoriteCity
import com.example.weatherforecastapplication.model.WeatherRepository
import com.example.weatherforecastapplication.model.WeatherRepositoryImpl
import com.example.weatherforecastapplication.network.RetrofitHelper
import com.example.weatherforecastapplication.network.WeatherRemoteDataSourceImpl
import com.example.weatherforecastapplication.network.WeatherService
import kotlinx.coroutines.launch


class FavFragment : Fragment() , OnCityClickListener {
    lateinit var recyclerView: RecyclerView
    lateinit var hourlyForecastAdapter: FavAdapter
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
        val favoriteCityLocalDataSource = FavoriteCityLocalDataSourceImpl(favoriteCityDao)

        val weatherRepository = WeatherRepositoryImpl.getInstance(
            WeatherRemoteDataSourceImpl.getInstance(
                RetrofitHelper.getInstance().create(WeatherService::class.java)
            ),localDataSource = favoriteCityLocalDataSource
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

                    hourlyForecastAdapter.submitList(state.weatherResponse)
                }
                is FavState.Failure -> {
                    Log.e("FavFragment", "cities has not been fetched")

                }
            }

        }}

    }

    private fun loadUserPreferences() {
        // Retrieve the language preference
        languageOption = sharedPreferences.getString("LANGUAGE", "en") ?: "en"
        unitOption = sharedPreferences.getString("TEMPERATURE_UNIT", "metric") ?: "metric"
        locationOption = sharedPreferences.getString("LOCATION_OPTION","gps") ?: "gps"

    }
    private fun initFavRecyclerView() {
        hourlyForecastAdapter = FavAdapter(this)
        binding.favouritesRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.favouritesRecyclerView.adapter = hourlyForecastAdapter
    }


    override fun onCityClick(city: FavoriteCity) {
        sharedViewModel.selectCity(city.cityName)
        fragmentNavigation.navigateToHome(city.cityName) // Navigate to HomeFragment with selected city



    }

    override fun onDeleteCityClick(city: FavoriteCity) {
        viewModel.deleteFavCity(city)
    }


}