package com.example.weatherforecastapplication.home.view

import android.content.Context
import androidx.recyclerview.widget.DiffUtil
import com.example.weatherforecastapplication.model.Forecast
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.databinding.ItemDailyForecastBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyForecastDiffUtil : DiffUtil.ItemCallback<Forecast>() {
    override fun areItemsTheSame(oldItem: Forecast, newItem: Forecast): Boolean {
        return oldItem.dt == newItem.dt
    }

    override fun areContentsTheSame(oldItem: Forecast, newItem: Forecast): Boolean {
        return oldItem == newItem
    }
}

// Adapter class for daily forecasts
class DailyForecastAdapter(private val languageOption: String) : ListAdapter<Forecast, DailyForecastAdapter.DailyViewHolder>(
    DailyForecastDiffUtil()
) {

    // ViewHolder for daily forecast items
    class DailyViewHolder(val binding: ItemDailyForecastBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyViewHolder {
        val binding = ItemDailyForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DailyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DailyViewHolder, position: Int) {
        val forecast = getItem(position)

        // Convert the timestamp to a Date object
        val date = Date(forecast.dt * 1000) // dt is in seconds, convert to milliseconds

        // Extract the day of the week (e.g., Monday)
        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val formattedDay = dayFormat.format(date)
        holder.binding.textDay.text = formattedDay

        // Set the Locale based on the language option
        val locale = if (languageOption == "ar") Locale("ar", "SA") else Locale.getDefault()


        // Set "Today" for the first item, otherwise use the actual day name
        val displayDay = if (position == 0) {
            // Check the language option
            if (languageOption == "ar") {
                "اليوم" // Arabic for "Today"
            } else {
                "Today" // Default to English
            }
        } else {
            dayFormat.format(date)
        }

        holder.binding.textDay.text = displayDay

        // Get temperature unit from SharedPreferences
        val temperatureOption = holder.binding.root.context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
            .getString("TEMPERATURE_UNIT", "metric") ?: "metric"

        // Format the temperature value with Arabic numerals if language is set to Arabic
        val temperatureFormat = NumberFormat.getInstance(locale)
        val temperature = when (temperatureOption) {
            "metric" -> {
                if (languageOption == "ar") "${temperatureFormat.format(forecast.main.temp.toInt())}°س" // Arabic Celsius
                else "${forecast.main.temp.toInt()}°C" // English Celsius
            }

            "imperial" -> {
                if (languageOption == "ar") "${temperatureFormat.format(forecast.main.temp.toInt())}°ف" // Arabic Fahrenheit
                else "${forecast.main.temp.toInt()}°F" // English Fahrenheit
            }
            else -> {
                if (languageOption == "ar") "${temperatureFormat.format(forecast.main.temp.toInt())} ك" // Arabic Kelvin
                else "${forecast.main.temp.toInt()} K" // English Kelvin
            }
        }

        holder.binding.textTemp.text = temperature

        // Load the weather icon (if applicable)
//        Glide.with(holder.binding.imageDailyWeatherIcon.context)
//            .load("https://openweathermap.org/img/wn/${forecast.weather[0].icon}@2x.png")
//            .into(holder.binding.imageDailyWeatherIcon)
    }
}