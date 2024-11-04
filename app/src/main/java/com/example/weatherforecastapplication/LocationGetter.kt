package com.example.weatherforecastapplication

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationGetter(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // Check if location permissions are granted
    fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Suspend function to get the current location
    suspend fun getLocation(): Location? {
        if (hasLocationPermission()) {
            return suspendCancellableCoroutine { continuation ->
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(location)
                    } else {
                        // Log the situation when location is null
                        Log.e("LocationGetter", "Location is null")
                        continuation.resume(null)
                    }
                }.addOnFailureListener { exception ->
                    Log.e("LocationGetter", "Failed to get location: ${exception.message}")
                    continuation.resume(null)
                }
            }
        } else {
            Log.e("LocationGetter", "Location permission not granted")
            return null // Indicate that permission is not granted
        }
    }
}