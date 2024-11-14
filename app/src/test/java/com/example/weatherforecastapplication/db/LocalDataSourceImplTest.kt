//package com.example.weatherforecastapplication.db
//
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
//import androidx.room.Room
//import androidx.test.core.app.ApplicationProvider
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import com.example.weatherforecastapplication.model.FavoriteCity
//
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.test.runTest
//import org.hamcrest.CoreMatchers.`is`
//import org.hamcrest.MatcherAssert.assertThat
//import org.junit.After
//import org.junit.Assert.assertTrue
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//
//@RunWith(AndroidJUnit4::class)
//@ExperimentalCoroutinesApi
//class LocalDataSourceImplTest {
//
//    @get:Rule
//    val instantTaskExecutorRule = InstantTaskExecutorRule()
//
//    private lateinit var database: AppDatabase
//    private lateinit var favoriteCityDao: FavoriteCityDao
//    private lateinit var favoriteCityDao2: WeatherDao
//    private lateinit var favoriteCityDao3: ForecastResponseDao
//
//    private lateinit var favoriteCityLocalDataSource: LocalDataSourceImpl
//
//
//    @Before
//    fun setup() {
//        // Initialize in-memory database
//        database = Room.inMemoryDatabaseBuilder(
//            ApplicationProvider.getApplicationContext(),
//            AppDatabase::class.java
//        ).allowMainThreadQueries()
//            .build()
//
//        // Get a reference to the DAO
//        favoriteCityDao = database.favoriteCityDao()
//        favoriteCityDao2 = database.weatherDao()
//        favoriteCityDao3 = database.forecastResponseDao()
//
//        // Initialize the local data source
//        favoriteCityLocalDataSource = LocalDataSourceImpl(favoriteCityDao, favoriteCityDao2, favoriteCityDao3)
//    }
//
//    @After
//    fun tearDown() {
//        // Close the database after each test
//        database.close()
//    }
//
//    @Test
//    fun insertFavoriteCity_retrievesFavoriteCity() = runTest {
//        // Step 1: Create a favorite city
//        val favoriteCity = FavoriteCity(cityName = "Cairo", country = "Egypt")
//
//        // Step 2: Save the favorite city
//        favoriteCityLocalDataSource.saveFavoriteCity(favoriteCity)
//
//        // Step 3: Retrieve the favorite city
//        val allCities = favoriteCityLocalDataSource.getFavoriteCities().first()
//
//        // Step 4: Check if the favorite city was retrieved correctly
//        assertThat(allCities.size, `is`(1))
//        assertThat(allCities[0].cityName, `is`("Cairo"))
//        assertThat(allCities[0].country, `is`("Egypt"))
//    }
//
//    @Test
//    fun deleteFavoriteCity_retrievesEmptyList() = runTest {
//        // Step 1: Create and save a favorite city
//        val favoriteCity = FavoriteCity(cityName = "Cairo", country = "Egypt")
//        favoriteCityLocalDataSource.saveFavoriteCity(favoriteCity)
//
//        // Step 2: Retrieve the saved favorite city to get its ID
//        val savedCities = favoriteCityLocalDataSource.getFavoriteCities().first()
//        val cityIdToDelete = savedCities[0].id // Get the ID of the saved city
//
//        // Step 3: Delete the favorite city by ID
//        favoriteCityLocalDataSource.deleteFavoriteCity(cityIdToDelete)
//
//        // Step 4: Retrieve all favorite cities
//        val allCities = favoriteCityLocalDataSource.getFavoriteCities().first()
//
//        // Step 5: Verify that the list of favorite cities is empty
//        assertTrue(allCities.isEmpty())
//    }
//}