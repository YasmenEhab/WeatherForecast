//package com.example.weatherforecastapplication.db
//
//import android.content.Context
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import androidx.test.core.app.ApplicationProvider
//import androidx.room.Room
//import com.example.weatherforecastapplication.model.FavoriteCity
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.runBlocking
//import kotlinx.coroutines.test.runTest
//import org.junit.After
//import org.junit.Assert.assertEquals
//import org.junit.Assert.assertTrue
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//
//@RunWith(AndroidJUnit4::class)
//class FavoriteCityDaoTest {
//    private lateinit var favoriteCityDao: FavoriteCityDao
//    private lateinit var db: AppDatabase
//
//    @Before
//    fun setUp() {
//        val context = ApplicationProvider.getApplicationContext<Context>()
//        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
//        favoriteCityDao = db.favoriteCityDao()
//    }
//
//    @After
//    fun tearDown() {
//        db.close()
//    }
//
//    @Test
//    fun insertAndGetFavoriteCity() = runTest {
//        // Create a sample task
//        val favoriteCity = FavoriteCity(cityName = "Cairo")
//
//        // Insert the task into the database
//        favoriteCityDao.insertFavoriteCity(favoriteCity)
//
//        // Retrieve the task from the database by ID
//        val allCities = favoriteCityDao.getAllFavoriteCities().first()
//
//        //  Optionally compare the actual task with the expected task
//        assertEquals(allCities.size, 1)
//
//    }
//
//    @Test
//    fun deleteFavoriteCity() = runTest {
//        val favoriteCity = FavoriteCity(cityName = "Cairo", country = "Egypt")
//        //favoriteCityDao.insertFavoriteCity(favoriteCity)
//
//        favoriteCityDao.deleteFavoriteCity(favoriteCity.id)
//
//        val allCities = favoriteCityDao.getAllFavoriteCities().first()
//
//        assertTrue(allCities.isEmpty())
//    }
//}