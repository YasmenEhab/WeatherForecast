package com.example.weatherforecastapplication.setting.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.databinding.FragmentSettingBinding
import java.util.Locale

class SettingFragment : Fragment() {

    private lateinit var binding: FragmentSettingBinding
    private lateinit var sharedPreferences: SharedPreferences


    companion object {
        private const val TAG = "SettingFragment" // Tag for logging
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("Settings", Context.MODE_PRIVATE)

        setupLanguageSpinner()
        setupTemperatureUnitSpinner()

//        setupLocationSpinner()
    }

    private fun setupLanguageSpinner() {
        val languageAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.languages_array,
            R.layout.spinner_item
        )
        languageAdapter.setDropDownViewResource(R.layout.spinner_item)
        binding.languageSpinner.adapter = languageAdapter

        // Load saved language and set selection
        var savedLanguage = sharedPreferences.getString("LANGUAGE", "en")
        Log.d(TAG, "Loaded saved language: $savedLanguage") // Log the saved language

        // Set selection based on saved language
        binding.languageSpinner.setSelection(if (savedLanguage == "ar") 1 else 0)


            // Set up listener for when an item in the spinner is selected
            binding.languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    // Determine the selected language based on position
                    val selectedLanguage = if (position == 0) "en" else "ar" // Correct mapping

                    // Only save and change language if it's different from the current
                    if (selectedLanguage != savedLanguage) {
                        savedLanguage = selectedLanguage
                        sharedPreferences.edit().putString("LANGUAGE", selectedLanguage).apply()
                        Log.d(TAG, "Saved language: $selectedLanguage") // Log after saving
                        changeLanguage(selectedLanguage)
                    }
                }


            override fun onNothingSelected(parent: AdapterView<*>) { }
        }
    }
    private fun changeLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = requireContext().resources.configuration
        config.setLocale(locale)

        requireContext().resources.updateConfiguration(config, requireContext().resources.displayMetrics)

        // Restart activity to apply changes
        val intent = requireActivity().intent
        requireActivity().finish()
        startActivity(intent)
    }


    private fun setupTemperatureUnitSpinner() {
        val tempUnitAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.temp_units_array,
            R.layout.spinner_item
        )
        tempUnitAdapter.setDropDownViewResource(R.layout.spinner_item)
        binding.tempUnitSpinner.adapter = tempUnitAdapter

        val savedTempUnit = sharedPreferences.getString("TEMPERATURE_UNIT", "metric")
        binding.tempUnitSpinner.setSelection(
            when (savedTempUnit) {
                "imperial" -> 1
                "standard" -> 2
                else -> 0 // metric
            }
        )

        binding.tempUnitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedTempUnit = when (position) {
                    1 -> "imperial"
                    2 -> "standard"
                    else -> "metric"
                }
                sharedPreferences.edit().putString("TEMPERATURE_UNIT", selectedTempUnit).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>) { }
        }
    }



    private fun setupLocationSpinner() {
        val locationAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.location_options_array,
            R.layout.spinner_item
        )
        locationAdapter.setDropDownViewResource(R.layout.spinner_item)
        binding.locationSpinner.adapter = locationAdapter

        val savedLocationOption = sharedPreferences.getString("LOCATION_OPTION", "gps")
        binding.locationSpinner.setSelection(
            if (savedLocationOption == "map") 1 else 0
        )

        binding.locationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedLocationOption = if (position == 1) "map" else "gps"
                sharedPreferences.edit().putString("LOCATION_OPTION", selectedLocationOption).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>) { }
        }
    }
}
