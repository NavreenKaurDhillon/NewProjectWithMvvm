package com.live.humanmesh.view.activity

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.live.humanmesh.database.AppSharedPreferences
import com.live.humanmesh.databinding.ActivitySplashBinding
import com.live.humanmesh.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
   /* private val locationUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val latitude = intent.getDoubleExtra("latitude", 0.0)
            val longitude = intent.getDoubleExtra("longitude", 0.0)

            if (latitude != 0.0 && longitude != 0.0) {
                // Handle the location update, e.g., call an API with the new location
                // callApiWithLocation(Location(latitude, longitude))
            }
        }
    }*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*// Register the receiver to listen for location updates
        val filter = IntentFilter("com.live.humanmesh.LOCATION_UPDATE")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // For Android 12 (API 31) and above, you need to specify the receiver export status.
            // You can use Context.RECEIVER_NOT_EXPORTED for local use within the app.
            registerReceiver(locationUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            // For Android versions below 12, simply register the receiver.
            registerReceiver(locationUpdateReceiver, filter)
        }*/

        if (intent.extras?.getString("type") != null && intent.extras?.containsKey("type") == true){
            startActivity(Intent(this@MainActivity, HomeActivity::class.java)
                .putExtra("type",intent.extras?.getString("type")))
        }
        else {
            CoroutineScope(Dispatchers.Main).launch {
                delay(2000)
                if (AppSharedPreferences.getFromDatabase(Constants.IS_LOGIN, false)) {
                    startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                    finishAffinity()
                } else {
                    val intent = Intent(this@MainActivity, AuthActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                }
            }
        }
    }
}

