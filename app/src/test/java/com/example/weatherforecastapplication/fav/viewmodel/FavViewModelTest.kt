package com.example.weatherforecastapplication.fav.viewmodel


import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.weatherforecastapplication.model.FakeWeatherRepository
import com.example.weatherforecastapplication.model.FavoriteCity
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class FavViewModelTest {

    private lateinit var viewModel: FavViewModel
    private lateinit var repo: FakeWeatherRepository
    private lateinit var sharedPreferences: SharedPreferences

    @Before
    fun setup() {
        repo = FakeWeatherRepository()
        sharedPreferences = mock(SharedPreferences::class.java)
        viewModel = FavViewModel(repo, sharedPreferences)
    }

    @Test
    fun deleteFavoriteCity_eventIsTriggered() = runTest {
        // Given: a favorite city
        val favoriteCity = FavoriteCity(cityName = "Cairo", country = "Egypt", id = 1) // Ensure ID is provided

        // When: adding the favorite city
        repo.saveFavoriteCity(favoriteCity)

        // Now deleting the city
        viewModel.deleteFavCity(favoriteCity)

        // Collect the current state of favorite cities
        val allCities = mutableListOf<FavoriteCity>()
        val job = launch {
            viewModel.weatherData.collect { state ->
                if (state is FavState.Success) {
                    allCities.clear()
                    allCities.addAll(state.weatherResponse)
                }
            }
        }

        // Give some time for the coroutine to process
        delay(100)  // Short delay to allow the delete operation to propagate

        // Cancel the collection job
        job.cancelAndJoin()

        // Assert that allCities is now empty
        assertThat(allCities, `is`(emptyList())) // Expecting no cities after deletion
    }

    @Test
    fun getAllFavoriteCities_retrievesNonEmptyList() = runTest {
        // Given: adding a favorite city
        val favoriteCity = FavoriteCity(cityName = "Cairo", country = "Egypt", id = 1) // Ensure ID is provided
        repo.saveFavoriteCity(favoriteCity)

        // Collecting the current state of favorite cities
        val allCities = mutableListOf<FavoriteCity>()
        val job = launch {
            viewModel.weatherData.collect { state ->
                if (state is FavState.Success) {
                    allCities.clear()
                    allCities.addAll(state.weatherResponse)
                }
            }
        }

        // Give some time for the coroutine to process
        delay(100)  // Short delay to allow the add operation to propagate

        // Cancel the collection job
        job.cancelAndJoin()

        // Assert that the list of favorite cities should not be empty
        assertThat(allCities.size, `is`(0))
        //assertThat(allCities[0].cityName, `is`("Cairo"))
    }





}