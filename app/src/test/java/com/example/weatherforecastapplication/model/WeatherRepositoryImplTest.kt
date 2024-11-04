package com.example.weatherforecastapplication.model

import com.example.weatherforecastapplication.db.FavoriteCityLocalDataSource
import com.example.weatherforecastapplication.network.WeatherRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.Before
import org.junit.Test

class WeatherRepositoryImplTest {
    private lateinit var remoteDataSource: WeatherRemoteDataSource
    private lateinit var localDataSource: FavoriteCityLocalDataSource
    private lateinit var repository: WeatherRepositoryImpl

    @Before
    fun setup() {
        // Mock remote data source returning a predefined weather response
        remoteDataSource = object : WeatherRemoteDataSource {
            override suspend fun getWeatherInfoOverNetwork(city: String, apiKey: String, units: String, lang: String): Flow<WeatherResponse> {
                val mockWeatherResponse = WeatherResponse(
                    coord = Coord(lon = 31.2357, lat = 30.0444),
                    weather = listOf(Weather(1, "Clear", "clear sky", "01d")),
                    main = Main(temp = 30.0, feels_like = 31.0, temp_min = 28.0, temp_max = 32.0, pressure = 1012, humidity = 60),
                    wind = Wind(speed = 5.0, deg = 180),
                    clouds = Clouds(all = 10),
                    name = "Cairo",
                    sys = Sys(country = "EG")
                )
                return flowOf(mockWeatherResponse)
            }

            override suspend fun getWeatherForecastOverNetwork(city: String, apiKey: String, units: String, lang: String): Flow<ForecastResponse> {
                // Similar mock data can be returned for forecast
                return flowOf(ForecastResponse(listOf(), City(name = "Cairo", country = "EG")))
            }
        } // Mock local data source with empty and predefined data
        localDataSource = object : FavoriteCityLocalDataSource {
            private val favoriteCities = mutableListOf<FavoriteCity>()

            override suspend fun saveFavoriteCity(city: FavoriteCity) {
                favoriteCities.add(city)
            }

            override suspend fun getFavoriteCities(): Flow<List<FavoriteCity>> {
                return flowOf(favoriteCities)
            }

            override suspend fun deleteFavoriteCity(cityId: Int) {
                favoriteCities.removeIf { it.id == cityId }
            }

            override suspend fun addFavoriteCity(cityName: String) {
                TODO("Not yet implemented")
            }
        }

        // Instantiate the repository with the mocked data sources
        repository = WeatherRepositoryImpl.getInstance(remoteDataSource, localDataSource)
    }

    @Test
    fun getWeatherInfo_returnsExpectedWeatherResponse() = runTest {
        // Given: a city name and a predefined weather response
        val cityName = "Cairo"
        val expectedWeatherResponse = WeatherResponse(
            coord = Coord(lon = 31.2357, lat = 30.0444),
            weather = listOf(Weather(1, "Clear", "clear sky", "01d")),
            main = Main(temp = 30.0, feels_like = 31.0, temp_min = 28.0, temp_max = 32.0, pressure = 1012, humidity = 60),
            wind = Wind(speed = 5.0, deg = 180),
            clouds = Clouds(all = 10),
            name = cityName,
            sys = Sys(country = "EG")
        )

        // Mock the remote data source to return the expected weather response
        (repository.remoteDataSource as WeatherRemoteDataSource).apply {
            // Override the method to return the predefined response
            val mockFlow = flowOf(expectedWeatherResponse)
            suspend fun getWeatherInfoOverNetwork(city: String, apiKey: String, units: String, lang: String): Flow<WeatherResponse> {
                return mockFlow
            }
        }

        // Collect the response in a coroutine
        val job = launch {
            repository.getWeatherInfo(cityName, "dummyApiKey", "metric", "en").collect { weatherResponse ->
                // Then: assert that the response is as expected
                assertThat(weatherResponse.name, IsEqual(expectedWeatherResponse.name))
                assertThat(weatherResponse.weather[0].description, IsEqual(expectedWeatherResponse.weather[0].description))
            }
        }

        // Wait for the coroutine to complete
        job.join()
    }

//    @Test
//    fun saveFavoriteCity_andRetrieveFavoriteCities() = runTest {
//        // Given: a favorite city
//        val favoriteCity = FavoriteCity(cityName = "Cairo", country = "Egypt", id = 1)
//
//        // When: saving the favorite city
//        repository.saveFavoriteCity(favoriteCity)
//
//        // Then: retrieving favorite cities should return the saved city
//        val allCities = repository.getFavoriteCities().first()
//        assertThat(allCities.size, IsEqual(1))
//        assertThat(allCities[0].cityName, IsEqual("Cairo"))
//    }
//
@Test
fun deleteFavoriteCity_removesCity() = runTest {
    // Given: a favorite city
    val favoriteCity = FavoriteCity(cityName = "Cairo", country = "Egypt", id = 1)
    repository.saveFavoriteCity(favoriteCity)

    // When: deleting the favorite city
    repository.deleteFavoriteCity(favoriteCity.id)

    // Collect the current state of favorite cities
    val job = launch {
        repository.getFavoriteCities().collect { allCities ->
            // Then: assert that the retrieved list of favorite cities is empty
            assertThat(allCities, IsEqual(emptyList<FavoriteCity>()))
        }
    }

    // Wait for the coroutine to complete
    job.join()
}
}
