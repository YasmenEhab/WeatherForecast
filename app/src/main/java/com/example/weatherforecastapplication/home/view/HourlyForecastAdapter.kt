import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.databinding.ItemHourlyForecastBinding
import com.example.weatherforecastapplication.model.Forecast
import java.text.NumberFormat
import java.text.SimpleDateFormat
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

    class HourlyViewHolder(val binding: ItemHourlyForecastBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyViewHolder {
        val binding = ItemHourlyForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HourlyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HourlyViewHolder, position: Int) {
        val forecast = getItem(position)

        // Convert the timestamp to a Date object
        val date = Date(forecast.dt * 1000) // dt is in seconds, convert to milliseconds

        // Retrieve the current language from SharedPreferences
        val sharedPreferences = holder.binding.root.context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val languageOption = sharedPreferences.getString("LANGUAGE", "en") ?: "en"
        val temperatureOption = sharedPreferences.getString("TEMPERATURE_UNIT", "metric") ?: "metric"

        // Set the Locale based on the language option
        val locale = if (languageOption == "ar") Locale("ar", "SA") else Locale.getDefault()


        // Extract the time from the forecast with the appropriate locale
        val dateFormat = SimpleDateFormat("h a", locale)
        val formattedTime = dateFormat.format(date)
        holder.binding.hourTextView.text = formattedTime

        // Set the temperature
       // holder.binding.tempTextView.text = "${forecast.main.temp.toInt()}°C"
        //holder.binding.tempTextView.text = holder.binding.root.context.getString(R.string.temperature_format, forecast.main.temp.toInt())
        // Set the temperature value and unit
        // Format the temperature with Arabic numerals if the language is set to Arabic
        val temperatureFormat = NumberFormat.getInstance(locale)

        // Set the temperature value and unit with Arabic numerals if Arabic is chosen
        val temperatureValue = when (temperatureOption) {
            "metric" -> {
                if (languageOption == "ar") {
                    "${temperatureFormat.format(forecast.main.temp.toInt())}°س" // Arabic Celsius
                } else {
                    "${forecast.main.temp.toInt()}°C" // English Celsius
                }
            }
            "imperial" -> {
                if (languageOption == "ar") {
                    "${temperatureFormat.format(forecast.main.temp.toInt())}°ف" // Arabic Fahrenheit
                } else {
                    "${forecast.main.temp.toInt()}°F" // English Fahrenheit
                }
            }
            else -> {
                if (languageOption == "ar") {
                    "${temperatureFormat.format(forecast.main.temp.toInt())} ك" // Arabic Kelvin
                } else {
                    "${forecast.main.temp.toInt()} K" // English Kelvin
                }
            }
        }

        holder.binding.tempTextView.text = temperatureValue

        Glide.with(holder.binding.imgTimeState.context)
            .load("https://openweathermap.org/img/wn/${forecast.weather[0].icon}@2x.png")
            .into(holder.binding.imgTimeState)
    }

}
