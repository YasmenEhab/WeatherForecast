package com.example.weatherforecastapplication.setting.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.databinding.FragmentSettingBinding


class SettingFragment : Fragment() {

    private lateinit var binding: FragmentSettingBinding
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize binding with the layout inflater
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Initialize SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("Settings", Context.MODE_PRIVATE)

        // Set up language spinner
        val spinnerAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.languages_array,
            R.layout.spinner_item
        )
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_item)
        binding.languageSpinner.adapter = spinnerAdapter

        // Load saved language and set selection
        val savedLanguage = sharedPreferences.getString("LANGUAGE", "en")
        binding.languageSpinner.setSelection(if (savedLanguage == "ar") 1 else 0)


        // Set listener for language selection
        binding.languageSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedLanguage = if (position == 0) "en" else "ar"
                    sharedPreferences.edit().putString("LANGUAGE", selectedLanguage).apply()

                    // Optional: notify ViewModel or reload data to apply language preference
                    // You might call your ViewModel here to trigger a data reload
                }
                override fun onNothingSelected(parent: AdapterView<*>) { /* No action */ }
            }

                val spinnerAdapter2 = ArrayAdapter.createFromResource(
                    requireContext(),
                    R.array.temp_units_array,
                    R.layout.spinner_item
                )
                spinnerAdapter2.setDropDownViewResource(R.layout.spinner_item)
                binding.tempUnitSpinner.adapter = spinnerAdapter2

                val spinnerAdapter3 = ArrayAdapter.createFromResource(
                    requireContext(),
                    R.array.location_options_array,
                    R.layout.spinner_item
                )
                spinnerAdapter3.setDropDownViewResource(R.layout.spinner_item)
                binding.locationSpinner.adapter = spinnerAdapter3

                val spinnerAdapter4 = ArrayAdapter.createFromResource(
                    requireContext(),
                    R.array.wind_speed_units_array,
                    R.layout.spinner_item
                )
                spinnerAdapter4.setDropDownViewResource(R.layout.spinner_item)
                binding.windSpeedUnitSpinner.adapter = spinnerAdapter4

            }


    }