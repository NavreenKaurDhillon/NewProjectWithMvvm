/*
package com.live.humanmesh.utils.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.live.humanmesh.utils.Constants
import com.live.humanmesh.view.activity.HomeActivity

class LocationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)

        if (latitude != 0.0 && longitude != 0.0) {
            // Call API or handle location here
            Log.d("LocationReceiver", "Received: $latitude, $longitude")
            val activityIntent = Intent(context, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(Constants.LATITUDE, latitude)
                putExtra(Constants.LONGITUDE, longitude)
            }
            context.startActivity(activityIntent)
            // You can also start a service or enqueue a work request here
        }
    }
}
*/
