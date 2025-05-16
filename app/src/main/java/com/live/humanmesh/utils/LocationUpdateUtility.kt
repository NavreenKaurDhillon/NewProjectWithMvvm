package com.live.humanmesh.utils

import android.Manifest
import android.app.ActionBar
import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.material.snackbar.Snackbar
import com.live.humanmesh.R
import com.live.humanmesh.database.AppSharedPreferences.getFromDatabase
import com.live.humanmesh.model.LocationData
import java.io.IOException
import java.lang.Exception
import java.util.Locale
import java.util.TimeZone
import kotlin.collections.all
import kotlin.collections.first
import kotlin.collections.forEach
import kotlin.collections.isNotEmpty
import kotlin.jvm.java
import kotlin.toString

abstract class LocationUpdateUtility<VB : ViewBinding> : Fragment() {

    var baseView: View? = null
    private var _binding: VB? = null
    val binding get() = _binding
    var isLoaded = false

    protected val TAG = "LocationUpdateUtility"
//    private lateinit var binding: ActivityMainBinding

    private lateinit var mActivity: Activity
    private var locationRequest: LocationRequest? = null
    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var locationCallback: LocationCallback

    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )



    /*override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        //   AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        super.onAttach(context)

    }*/
    @LayoutRes
    protected abstract fun getLayoutRes(): Int
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, getLayoutRes(), container, false)

        return binding?.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

//    abstract fun getViewBinding(): VB

    //called first to check permissions status
    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
           // if (permissions.isNotEmpty()) {

            permissions.entries.forEach {
                Log.d(TAG, "${it.key} = ${it.value}")
            }
            val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION]
            val coarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION]

            if (fineLocation == true && coarseLocation == true) {
                Log.e(TAG, "Permission Granted Successfully")
                checkGpsOn()
            } else {
                Log.e(TAG, "Permission not granted"+permissions)
                if (permissions.keys.size > 0)
                    checkPermissionDenied(permissions.keys.first())
            }
        }

    private val gpsOnLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                Log.e(TAG, "GPS Turned on successfully")
             //   showProgress()
                startLocationUpdates()
            } else if (result.resultCode == RESULT_CANCELED) {
                Log.e(TAG, "GPS Turned on failed")
                // showRationaleDialog()
                Snackbar.make(requireActivity().findViewById(android.R.id.content), "GPS is required you to be turned on", Snackbar.LENGTH_LONG).setAction("ok") {
                    // Request GPS On
                    checkGpsOn()
                }.show()


            }
        }


    open fun getLiveLocation(activity: Activity) {

        mActivity = activity

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(mActivity)

        checkLocationPermissions()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    // Update UI with location data
                    // ...
                    try {
                        val addresses: List<Address>?
                        val geoCoder = Geocoder(activity, Locale.getDefault())
                        if (geoCoder != null) {
                            Log.d(TAG, "onLocationResult: // not null"+geoCoder.toString())
                            addresses = geoCoder.getFromLocation(
                                locationResult.lastLocation!!.latitude,
                                locationResult.lastLocation!!.longitude,
                                1)
                            if (addresses != null && addresses.isNotEmpty()) {
                                Log.d(TAG, "onLocationResultbvvv: "+addresses[0])
                                val address: String = addresses[0].getAddressLine(0)
                                val city: String = addresses[0].locality
                                val state: String = addresses[0].adminArea
                                val country: String = addresses[0].countryName
                                val postalCode: String = addresses[0].postalCode
                                val knownName: String = addresses[0].featureName
                                try {
                                    val subl = addresses[0].subLocality.toString()
                                    Log.d(TAG, "onLocationResult: sub "+subl)

                                }
                                catch (e: Exception){
                                    Log.d(TAG, "onLocationResult: subrv ")
                                }

                                val timeZone = getTimeZoneFromLocation(location)
                                val locationData = LocationData(
                                    location.latitude,
                                    location.longitude,
                                    state,
                                    country,
                                    city,
                                    timeZone.toString())
                                updatedLatLng(locationData)
                            }
                        }
                    } catch (e: IOException) {
                        Log.d(TAG, "onLocationResult: // "+e.message.toString())
                        e.printStackTrace()
                    }
                    //remove if req  continuousu updates
//                 stopLocationUpdates()
                    Log.d(TAG,
                        "==========" + location.latitude.toString() + ", " + location.longitude + "=========")
                }
            }
        }
//        stopLocationUpdates()
    }

    private fun getTimeZoneFromLocation(location: Location?): String? {
        val tz = TimeZone.getTimeZone(TimeZone.getDefault().id)
        Log.d(TAG, "getTimeZoneFromLocation: //"+tz.id)
        val offset = tz.getOffset(location?.time!!)
        return tz.id
    }

    private fun checkLocationPermissions() {
        if (hasPermissions(permissions)) {
            Log.d(TAG, "Permissions Granted")
            checkGpsOn()
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            checkPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            checkPermissionDenied(Manifest.permission.ACCESS_COARSE_LOCATION)
        } else {
            Log.d(TAG, "Request for Permissions")
            requestPermission()
        }
    }

    // util method
    private fun hasPermissions(permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(mActivity, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        requestMultiplePermissions.launch(permissions)
    }

    private fun checkPermissionDenied(permission: String) {

        val mActivity = requireActivity()
        if (shouldShowRequestPermissionRationale(permission)) {
            val mBuilder = AlertDialog.Builder(mActivity)
            val dialog: AlertDialog =
                mBuilder.setTitle("Permissions Required").setMessage(R.string.location_rationale)
                    .setPositiveButton(
                        R.string.ok
                    ) { dialog, which -> requestPermission() }
                  .create()
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    ContextCompat.getColor(
                        mActivity!!, R.color.red_color
                    )
                )
            }
            dialog.show()
        }
        else {
            val builder = AlertDialog.Builder(mActivity)
            val dialog: AlertDialog =
                builder.setTitle("Permissions Required").setMessage(R.string.location_rationale)
                    .setCancelable(
                        false
                    )
                    .setPositiveButton(R.string.openSettings) { dialog, which ->
                        //finish()
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts(
                                "package",
                                "com.live.humanmesh",
                                null))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }.create()
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    ContextCompat.getColor(
                        mActivity!!, R.color.red_color
                    )
                )
            }
            dialog.show()
        }
//        if (shouldShowRequestPermissionRationale(permission)) {
//            Log.e(TAG, "Permissions Denied"+permission)
//            Snackbar.make(
//                requireActivity().findViewById(android.R.id.content),
//                R.string.permission_rationale,
//                Snackbar.LENGTH_LONG
//            )
//                .setAction(R.string.ok) {
//                    // Request permission
//                    requestPermission()
//                }.show()
//
//
//        }
//        else {
//            Snackbar.make(
//                requireActivity().findViewById(android.R.id.content),
//                R.string.permission_denied_explanation,
//                Snackbar.LENGTH_LONG)
//                .setAction(R.string.settings) {
//                    // Build intent that displays the App settings screen.
//                    val intent = Intent()
//                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                    val uri = Uri.fromParts(
//                        "package",
//                        APPLICATION_ID,
//                        null)
//                    intent.data = uri
//                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                    startActivity(intent)
//                }.show()
//        }
    }

    private fun locationPermission(permissions: Array<String>): Boolean {
        return ActivityCompat.checkSelfPermission(
            mActivity,
            permissions[0]
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            mActivity,
            permissions[1]
        ) == PackageManager.PERMISSION_GRANTED
    }



    //func that checks if device location is turned on or not
    private fun checkGpsOn() {
        locationRequest = LocationRequest.create()
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY //PRIORITY_BALANCED_POWER_ACCURACY
        locationRequest?.interval = 5000
        locationRequest?.fastestInterval = 2000

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest!!)
        builder.setAlwaysShow(true)

        val result = LocationServices.getSettingsClient(mActivity)
            .checkLocationSettings(builder.build())

        result.addOnCompleteListener { task ->
            try {
                val response = task.getResult(ApiException::class.java)
                Log.e(TAG, "==========GPS is ON=============")

                //2. after permission is granted start updates -> checks gps status and request loc turn on
                startLocationUpdates()
            } catch (e: ApiException) {
                when (e.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        val resolvableApiException = e as ResolvableApiException
                        gpsOnLauncher.launch(
                            IntentSenderRequest.Builder(resolvableApiException.resolution).build())
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    }
                }
            }
        }
    }

    //call startLocationUpdates() method for start live location update
    fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                mActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                mActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            hasPermissions(permissions)
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest!!, locationCallback,
            Looper.getMainLooper())
    }

    //call stopLocationUpdates() method for stop live location update
    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.e(TAG, "Get Live Location Stop")
    }

    abstract fun updatedLatLng(loc: LocationData)
    abstract fun showProgress()

}
