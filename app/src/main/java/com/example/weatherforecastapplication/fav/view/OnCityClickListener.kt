package com.example.weatherforecastapplication.fav.view

import com.example.weatherforecastapplication.model.FavoriteCity

interface OnCityClickListener {
    fun onCityClick(city: FavoriteCity)
    fun onDeleteCityClick(city: FavoriteCity)  // Click to delete city

}