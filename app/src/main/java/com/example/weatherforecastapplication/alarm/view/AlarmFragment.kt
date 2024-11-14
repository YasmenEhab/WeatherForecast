package com.example.skycast.alert.view

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TimePicker
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherforecastapplication.LocationGetter
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.alarm.broadcast.AlarmReceiver
import com.example.weatherforecastapplication.alarm.view.Alarm
import com.example.weatherforecastapplication.alarm.view.AlarmAdapter
import com.example.weatherforecastapplication.alarm.viewmodel.AlarmViewModel
import com.example.weatherforecastapplication.alarm.viewmodel.AlarmViewModelFactory
import com.example.weatherforecastapplication.db.AppDatabase
import com.example.weatherforecastapplication.db.LocalDataSourceImpl
import com.example.weatherforecastapplication.model.WeatherRepository
import com.example.weatherforecastapplication.model.WeatherRepositoryImpl
import com.example.weatherforecastapplication.network.RetrofitHelper
import com.example.weatherforecastapplication.network.WeatherRemoteDataSourceImpl
import com.example.weatherforecastapplication.network.WeatherService
import com.example.weatherforecastapplication.setting.SettingsManager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class AlarmFragment : Fragment() {
    private lateinit var viewModel: AlarmViewModel
    private lateinit var alarmAdapter: AlarmAdapter
    private lateinit var locationGetter: LocationGetter
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_alarm, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.alertsRecyclerView)
        val btnAddAlarm: Button = view.findViewById(R.id.btnAddAlert)

        // Create the WeatherRepository instance or inject it as needed
        // Initialize the repository for fetching weather data
        val favoriteCityDao = AppDatabase.getDatabase(requireContext()).favoriteCityDao()
        val weatherDao = AppDatabase.getDatabase(requireContext()).weatherDao()
        val forecastDao = AppDatabase.getDatabase(requireContext()).forecastResponseDao()
        val favoriteCityLocalDataSource = LocalDataSourceImpl(favoriteCityDao,weatherDao,forecastDao)
        val remoteDataSource = WeatherRemoteDataSourceImpl.getInstance(
            RetrofitHelper.getInstance().create(WeatherService::class.java)
        )
        weatherRepository = WeatherRepositoryImpl.getInstance(remoteDataSource, favoriteCityLocalDataSource,requireContext())
        sharedPreferences = requireContext().getSharedPreferences("Settings", Context.MODE_PRIVATE)

        // Create the ViewModelFactory with necessary parameters
        val viewModelFactory = AlarmViewModelFactory(weatherRepository,requireContext() )

        // Use ViewModelProvider with the factory
        viewModel = ViewModelProvider(this, viewModelFactory).get(AlarmViewModel::class.java)

        viewModel.fetchAlarms()

        alarmAdapter = AlarmAdapter(emptyList()) { alarm ->
            deleteAlarm(alarm)
        }

        recyclerView.adapter = alarmAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        setObservables()

        btnAddAlarm.setOnClickListener {
            fetchLocationAndShowDialog()
        }

        checkAlarmPermissions()

        return view
    }

    private fun deleteAlarm(alarm: Alarm) {
        // Delete the alarm from the ViewModel
        viewModel.deleteAlarm(alarm)

        // Stop the scheduled alarm in AlarmManager
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra("alarmId", alarm.id)
        }

        val requestCode = alarm.name.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // Use NO_CREATE to get the existing PendingIntent without creating a new one
        )

        // Cancel the alarm
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel() // Also cancel the PendingIntent
        }

        // Optionally, you can notify the user
        Toast.makeText(requireContext(), "Alarm deleted", Toast.LENGTH_SHORT).show()

        // Optionally, refresh the alarms list after deletion
        Log.d("AlarmFragment", "Calling fetchAlarms()")
        viewModel.fetchAlarms()
    }

    private fun checkAlarmPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
    }

    private fun fetchLocationAndShowDialog() {
        val locationGetter = LocationGetter(requireContext())
        if (locationGetter.hasLocationPermission()) {
            // Use a coroutine to fetch the location
            viewLifecycleOwner.lifecycleScope.launch {
                val location = locationGetter.getLocation()
                if (location != null) {
                    val cityName = getCityNameFromCoordinates(location.latitude, location.longitude)

                    val unit = sharedPreferences.getString("unit", "metric") ?: "metric"
                    val language = sharedPreferences.getString("language", "en") ?: "en"


                    // Fetch weather after getting location
                    viewModel.fetchWeather2(location.latitude, location.longitude, unit, language)
                    showAlarmDialog(location.latitude, location.longitude)
                } else {
                    Toast.makeText(requireContext(), "Unable to get location. Check permissions.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "Location permission not granted.", Toast.LENGTH_SHORT).show()
        }
    }
    private fun getCityNameFromCoordinates(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        // Safely retrieve the list of addresses
        val addresses: List<Address>? = try {
            geocoder.getFromLocation(latitude, longitude, 1)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        return if (addresses != null && addresses.isNotEmpty()) {
            val address = addresses[0]
            address.locality ?: address.subAdminArea ?: "Unknown City"
        } else {
            "Unknown City"
        }
    }

    private fun showAlarmDialog(latitude: Double, longitude: Double) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_alarm, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.alarmNamePicker)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.alarmTimePicker)

        timePicker.setIs24HourView(false)

        AlertDialog.Builder(requireContext())
            .setTitle("Set Alarm")
            .setView(dialogView)
            .setPositiveButton("Set") { _, _ ->
                val hour = timePicker.hour
                val minute = timePicker.minute
                val name = nameEditText.text.toString()

                // Create Alarm object
                val alarm = Alarm(
                    name = name,
                    time = "$hour:$minute",
                    hour = hour,
                    minute = minute,
                    latitude = latitude,
                    longitude = longitude
                )
                viewModel.addAlarm(alarm)
                viewModel.alarms.value?.add(alarm)

                // Fetch all alarms to update the list
                viewModel.fetchAlarms() // Fetch alarms after adding a new one
                viewModel.alarms.value?.let { alarmAdapter.updateAlarms(it) }

                // Schedule the alarm
                scheduleAlarm(alarm)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun scheduleAlarm(alarm: Alarm) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra("alarmId", alarm.id)
            putExtra("alarmName", alarm.name)
            putExtra("latitude", alarm.latitude)
            putExtra("longitude", alarm.longitude)
            putExtra("hour", alarm.hour)
            putExtra("minute", alarm.minute)
        }

        // Use the hash code of the alarm name for a unique request code
        val requestCode = alarm.name.hashCode()
        Log.d("AlarmFragment", "Scheduling alarm with requestCode: $requestCode")

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set the calendar time for the alarm
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)

            // If the time is in the past, set it for the next day
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        Log.d("AlarmFragment", "Alarm set for: ${calendar.time}")

        // Schedule the alarm
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)

        // Optionally, save the PendingIntent for future reference
        viewModel.savePendingIntent(requestCode, pendingIntent)
    }

    fun setObservables() {

        viewModel.alarms.observe(viewLifecycleOwner) { alarms ->
            Log.d("AlarmFragment", "Updating alarm adapter with alarms: ${alarms.size}")

            alarmAdapter.updateAlarms(alarms)
        }
    }
}
