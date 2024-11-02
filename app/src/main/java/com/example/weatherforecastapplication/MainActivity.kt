package com.example.weatherforecastapplication

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.weatherforecastapplication.alarm.view.AlarmFragment
import com.example.weatherforecastapplication.databinding.ActivityMainBinding
import com.example.weatherforecastapplication.fav.view.FavFragment
import com.example.weatherforecastapplication.home.view.HomeFragment
import com.example.weatherforecastapplication.map.SharedViewModel
import com.example.weatherforecastapplication.setting.view.SettingFragment
interface FragmentNavigation {
    fun navigateToHome(cityName: String)
}
class MainActivity : AppCompatActivity(), FragmentNavigation {

    private lateinit var binding: ActivityMainBinding
    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        replaceFragment(HomeFragment())// Set initial fragment

        // Set up the bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment())
                R.id.nav_favorites -> replaceFragment(FavFragment())
                R.id.nav_alarm -> replaceFragment(AlarmFragment())
                R.id.nav_setting -> replaceFragment(SettingFragment())
                else -> false
            }
            true
        }
    }


    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.nav_host_fragment, fragment) // Ensure this ID is correct
        fragmentTransaction.commit()
    }

    override fun navigateToHome(cityName: String) {
        val homeFragment = HomeFragment().apply {
            arguments = Bundle().apply {
                putString("city_name", cityName) // Pass the city name as an argument
            }
        }
        replaceFragment(homeFragment) // Replace with HomeFragment
    }


}