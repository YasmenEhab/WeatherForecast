package com.example.weatherforecastapplication.home.view

import androidx.recyclerview.widget.DiffUtil
import com.example.weatherforecastapplication.model.Forecast
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.databinding.ItemDailyForecastBinding
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

        // Set the high and low temperatures
        holder.binding.textTemp.text = holder.binding.root.context.getString(R.string.temperature_format, forecast.main.temp.toInt())


        // Load the weather icon (if applicable)
//        Glide.with(holder.binding.imageDailyWeatherIcon.context)
//            .load("https://openweathermap.org/img/wn/${forecast.weather[0].icon}@2x.png")
//            .into(holder.binding.imageDailyWeatherIcon)
    }
}