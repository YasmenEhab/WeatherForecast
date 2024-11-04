package com.example.weatherforecastapplication.alarm.view

import android.app.AlarmManager
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.alarm.viewmodel.AlarmViewModel

class AlarmAdapter(
    private val alarmList: MutableList<Alarm>,
    private val alarmViewModel: AlarmViewModel,
    private val onDeleteAlarm: (Alarm) -> Unit // Callback to delete the alarm
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {
    class AlarmViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val alarmName: TextView = view.findViewById(R.id.alarmName)
        val alarmTime: TextView = view.findViewById(R.id.alarmTime)
        val deleteButton: ImageView =
            view.findViewById(R.id.btnDelete) // Button to delete the alarm

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alarm, parent, false)
        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = alarmList[position]
        Log.d("AlarmAdapter", "Binding alarm: ${alarm.name} at ${alarm.time}") // Add this line

        holder.alarmName.text = alarm.name
       //  holder.alarmTime.text = alarm.getFormattedTime() // Use the new method to get formatted time
//        holder.alarmSwitch.isChecked = alarm.isActive
//
//        holder.alarmSwitch.setOnCheckedChangeListener { _, isChecked ->
//            if (holder.alarmSwitch.isPressed) { // Only toggle if user initiated
//                alarm.isActive = isChecked
//                if (isChecked) {
//                    alarmViewModel.setAlarm(alarm) // Set the alarm if turned on
//                } else {
//                    alarmViewModel.cancelAlarm(alarm) // Cancel the alarm if turned off
//                }
//                Log.d("AlarmAdapter", "Alarm ${alarm.name} is now ${if (isChecked) "active" else "inactive"}")
//            }
//        }
        holder.deleteButton.setOnClickListener {
            onDeleteAlarm(alarm) // Call the delete function
        }
    }

    override fun getItemCount(): Int = alarmList.size

    fun updateAlarms(newAlarms: List<Alarm>) {
        alarmList.clear()
        alarmList.addAll(newAlarms)
        Log.d("AlarmAdapter", "Updating alarms, new count: ${newAlarms.size}") // Add this line

        notifyDataSetChanged()
    }
}
