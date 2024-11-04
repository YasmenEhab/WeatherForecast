package com.example.weatherforecastapplication.alarm.view

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.alarm.broadcast.AlarmReceiver
import com.example.weatherforecastapplication.alarm.viewmodel.AlarmViewModel
import com.example.weatherforecastapplication.alarm.viewmodel.AlarmViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class AlarmFragment : Fragment() {
    private lateinit var alarmManager: AlarmManager
    private val alarmViewModel: AlarmViewModel by viewModels { AlarmViewModelFactory(requireContext()) }
    private lateinit var recyclerView: RecyclerView
    private lateinit var alarmAdapter: AlarmAdapter
    private val alarmList = mutableListOf<Alarm>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Observe the alarm status LiveData

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_alarm, container, false)
        recyclerView = view.findViewById(R.id.alertsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize the AlarmAdapter with toggle and delete actions
        alarmAdapter  = AlarmAdapter(alarmList, alarmViewModel) { alarm ->
            alarmViewModel.cancelAlarm(alarm)
        }

        recyclerView.adapter = alarmAdapter

        // Observe LiveData
        alarmViewModel.alarms.observe(viewLifecycleOwner) { alarms ->
            alarmAdapter.updateAlarms(alarms)
        }



        // Initialize AlarmManager
        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Set an alarm on button click
        view.findViewById<Button>(R.id.btnAddAlert).setOnClickListener {
            showAlarmDialog()
        }

        // Check if permission is granted when fragment is created
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmViewModel.isExactAlarmPermissionGranted()) {
            alarmViewModel.requestExactAlarmPermission()
        }

        return view
    }


    private fun toggleAlarm(alarm: Alarm, isActive: Boolean) {
        alarm.isActive = isActive
        if (isActive) {
            // Logic to set the alarm
            // alarmViewModel.scheduleAlarm(...)
        } else {
            // Logic to cancel the alarm
            // alarmViewModel.cancelAlarm(...)
        }
    }
    private fun deleteAlarm(alarm: Alarm) {
        alarmViewModel.cancelAlarm(alarm) // Ensure to cancel the alarm in ViewModel
    }


    private fun showAlarmDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_alarm, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.alarmNamePicker)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.alarmTimePicker)

        timePicker.setIs24HourView(true)

        AlertDialog.Builder(requireContext())
            .setTitle("Set Alarm")
            .setView(dialogView)
            .setPositiveButton("Set") { _, _ ->
                val hour = timePicker.hour
                val minute = timePicker.minute
                val name = nameEditText.text.toString()
                val time = String.format("%02d:%02d", hour, minute)

                val alarm = Alarm(
                    id = System.currentTimeMillis(), // Unique ID
                    name = name,
                    time = time,
                    timeInMillis = calculateTimeInMillis(hour, minute), // Calculate time in milliseconds
                    isActive = true // Default to active
                )
               // alarmViewModel.addAlarm(alarm) // Add to ViewModel

                Log.d("AlarmFragment", "Alarm added: $alarm") // Debug log
                alarmViewModel.scheduleAlarm(hour, minute, name)
                Toast.makeText(requireContext(), "Alarm scheduled for $name at $time", Toast.LENGTH_SHORT).show()


            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun calculateTimeInMillis(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1) // Schedule for the next day if the time is in the past
            }
        }
        return calendar.timeInMillis // Return time in milliseconds
    }
    override fun onResume() {
        super.onResume()
        // Notify user about permission status if required
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmViewModel.isExactAlarmPermissionGranted()) {
            Toast.makeText(requireContext(), "Permission granted! You can now set alarms.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            // Handle the result of the permission request
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Permission granted! You can now set alarms.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Permission denied! Unable to set alarms.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 1001 // Define your request code
    }
}
