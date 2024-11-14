//package com.example.weatherforecastapplication.model
//
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.flowOf
//
//class FakeWeatherRepository : WeatherRepository {
//    private val favoriteCities = mutableListOf<FavoriteCity>()
//
//    override suspend fun getWeatherInfo(city: String, apiKey: String, units: String, lang: String): Flow<WeatherResponse>
//    {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun getThreeHourForecast(city: String, apiKey: String, units: String, lang: String): Flow<ForecastResponse>
//    {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun getDailyForecast(city: String, apiKey: String, units: String, lang: String): Flow<ForecastResponse>
//    {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun saveFavoriteCity(city: FavoriteCity) {
//        favoriteCities.add(city)
//    }
//
//    override suspend fun getFavoriteCities(): Flow<List<FavoriteCity>> {
//        return flowOf(favoriteCities.toList())
//    }
//
//    override suspend fun deleteFavoriteCity(cityId: Int) {
//        favoriteCities.removeIf { it.id == cityId }
//    }
//}
