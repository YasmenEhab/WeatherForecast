package com.example.weatherforecastapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.weatherforecastapplication.alarm.view.AlarmFragment
import com.example.weatherforecastapplication.databinding.ActivityMainBinding
import com.example.weatherforecastapplication.fav.view.FavFragment
import com.example.weatherforecastapplication.home.view.HomeFragment
import com.example.weatherforecastapplication.setting.view.SettingFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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


}