package com.example.weatherforecastapplication.home.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.model.Forecast
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


class ForecastDiffUtil : DiffUtil.ItemCallback<Forecast>() {
    override fun areItemsTheSame(oldItem: Forecast, newItem: Forecast): Boolean {
        return oldItem.dt == newItem.dt
    }

    override fun areContentsTheSame(oldItem: Forecast, newItem: Forecast): Boolean {
        return oldItem == newItem
    }
}

class HourlyForecastAdapter : ListAdapter<Forecast, HourlyForecastAdapter.HourlyViewHolder>(
    ForecastDiffUtil()
) {

    class HourlyViewHolder(private val itemView: View) : RecyclerView.ViewHolder(itemView) {
        val hourTextView: TextView = itemView.findViewById(R.id.hourTextView)
        val tempTextView: TextView = itemView.findViewById(R.id.tempTextView)
       // val weatherIconImageView: ImageView = itemView.findViewById(R.id.image_weather_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_hourly_forecast, parent, false)
        return HourlyViewHolder(view)
    }

    override fun onBindViewHolder(holder: HourlyViewHolder, position: Int) {
        val forecast = getItem(position)

        // Convert the timestamp to a Date object
        val date = Date(forecast.dt * 1000) // dt is in seconds, convert to milliseconds

        // Extract the time from the forecast (formatted to show hours)
        val dateFormat = SimpleDateFormat("h a", Locale.getDefault())
        val formattedTime = dateFormat.format(date)
        holder.hourTextView.text = formattedTime

        // Set the temperature
        holder.tempTextView.text = "${forecast.main.temp.toInt()}°C"

        // You can set weather icons here (if applicable)
//        Glide.with(holder.weatherIconImageView.context)
//            .load("https://openweathermap.org/img/wn/${forecast.weather[0].icon}@2x.png")
//            .into(holder.weatherIconImageView)
    }
}