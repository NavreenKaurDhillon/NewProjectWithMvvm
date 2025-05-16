package com.live.humanmesh.utils.location

import android.Manifest
import android.app.Activity
import com.google.android.gms.location.LocationResult
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.live.humanmesh.R
import com.live.humanmesh.database.AppSharedPreferences.saveIntoDatabase
import com.live.humanmesh.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class LocationUpdateService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "location_channel",
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check for permissions before requesting location updates
        if (hasLocationPermissions()) {
            createNotificationChannel() // Make sure to call this

            val notification = NotificationCompat.Builder(this, "location_channel")
                .setContentTitle("Tracking Location")
                .setContentText("Your location is being monitored.")
                .setSmallIcon(R.drawable.ic_location) // Make sure this exists
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
            startForeground(1, notification) // MUST be called immediately
            startLocationUpdates()
        } else {
            requestLocationPermissions()
        }

        return START_STICKY
    }

    private fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        val intent = Intent(this, PermissionActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun startLocationUpdates() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000 // 10 seconds
            fastestInterval = 5000 // 5 seconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.let { locations ->
                    for (location in locations) {
                        updateLocation(location)
                    }
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                mainLooper
            )
        } catch (e: SecurityException) {
            Log.e("LocationService", "Location permission is not granted", e)
        }
    }

    private fun updateLocation(location: Location) {
        val addressBuilder = StringBuilder()
        Log.d("wekjfbjkbwef", "loc live = Latitude: ${location.latitude}, Longitude: ${location.longitude}")
        // Send location data to the activity using an Intent
        val geocoder = Geocoder(applicationContext, Locale.getDefault())
        val addresses = geocoder.getFromLocation(
            location.latitude, location.longitude.toDouble(),
            1
        ) as List<Address> // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        if (addresses.isNotEmpty()) {
            val address = addresses[0]
            address.subLocality?.let { addressBuilder.append("$it, ") }
            address.thoroughfare?.let { addressBuilder.append("$it, ") }
            address.locality?.let { addressBuilder.append("$it, ") }
            address.countryName?.let { addressBuilder.append("$it, ") }
            address.postalCode?.let { addressBuilder.append(it) }
        }
        val intent = Intent("com.live.humanmesh.LOCATION_UPDATE")
        intent.putExtra("latitude", location.latitude)
        intent.putExtra("longitude", location.longitude)
        intent.putExtra("address", addressBuilder.toString())
        sendBroadcast(intent)
    }

    // Inner data class to represent the location data sent to the API
    data class LocationData(val latitude: Double, val longitude: Double)

    companion object {
        const val PERMISSION_REQUEST_CODE = 1
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
