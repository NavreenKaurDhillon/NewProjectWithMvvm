package com.live.humanmesh.view.fragment

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.live.humanmesh.R
import com.live.humanmesh.apiservice.Resource
import com.live.humanmesh.apiservice.Status
import com.live.humanmesh.base.BaseApplication
import com.live.humanmesh.database.AppSharedPreferences.getFromDatabase
import com.live.humanmesh.database.AppSharedPreferences.saveIntoDatabase
import com.live.humanmesh.databinding.FragmentHomeBinding
import com.live.humanmesh.model.BannerItem
import com.live.humanmesh.model.BannersListResponse
import com.live.humanmesh.model.CommonResponse
import com.live.humanmesh.model.EventBody
import com.live.humanmesh.model.LocationData
import com.live.humanmesh.model.RequestListResponse
import com.live.humanmesh.utils.AppUtils.distanceBetweenInMeters
import com.live.humanmesh.utils.AppUtils.gone
import com.live.humanmesh.utils.AppUtils.visible
import com.live.humanmesh.utils.Constants
import com.live.humanmesh.utils.Constants.ACCEPT_REJECT_REQUEST
import com.live.humanmesh.utils.Constants.UPDATE_LAT_LNG
import com.live.humanmesh.utils.LocationUpdateUtility
import com.live.humanmesh.utils.Progress
import com.live.humanmesh.utils.showErrorAlert
import com.live.humanmesh.utils.showSuccessAlert
import com.live.humanmesh.view.activity.HomeActivity
import com.live.humanmesh.view.adapter.BannerViewPager
import com.live.humanmesh.view.adapter.RequestsAdapter
import com.live.humanmesh.viewmodel.AppViewModel
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class HomeFragment : LocationUpdateUtility<FragmentHomeBinding>(), Observer<Resource<JsonElement>> {

    private lateinit var homeBinding: FragmentHomeBinding
    override fun getLayoutRes(): Int = R.layout.fragment_home
    private lateinit var appViewModel: AppViewModel
    val addressBuilder = StringBuilder()
    private var adView: AdView? = null
    private var lastLatitude: Double? = null
    private var lastLongitude: Double? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            Snackbar.make(
                requireActivity().findViewById(android.R.id.content),
                R.string.notification_rationale,
                Snackbar.LENGTH_LONG
            )
                .setAction(R.string.ok) {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        102
                    )
                }.show()
//            showErrorAlert(getString(R.string.permission_to_show_notifications_is_not_granted))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeBinding = FragmentHomeBinding.inflate(inflater)
        return homeBinding.root
    }

    override fun updatedLatLng(loc: LocationData) {
        Log.d("wekjfbjkbwef", "updatedLatLng: 343434g34t34")
        loc?.let {
            val newLat = it.latitude
            val newLng = it.longitude
            if (lastLatitude == null || lastLongitude == null ||
                distanceBetweenInMeters(lastLatitude!!, lastLongitude!!, newLat, newLng) >= 10
            ) {
                // Update location only if moved at least 10 meters
                lastLatitude = newLat
                lastLongitude = newLng
                saveIntoDatabase(Constants.LATITUDE, loc.latitude.toString())
                saveIntoDatabase(Constants.LONGITUDE, loc.longitude.toString())
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addresses = geocoder.getFromLocation(
                    loc.latitude, loc.longitude.toDouble(),
                    1
                ) as List<Address> // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                if (addresses.isNotEmpty()) {
                    val address = addresses[0]
                    address.subLocality?.let { addressBuilder.append("$it, ") }
                    address.thoroughfare?.let { addressBuilder.append("$it, ") }
                    address.locality?.let { addressBuilder.append("$it, ") }
                    address.countryName?.let { addressBuilder.append("$it, ") }
                    address.postalCode?.let { addressBuilder.append(it) }
                    binding?.locTV?.text = addressBuilder.toString()
                    saveIntoDatabase(Constants.USER_LOCATION, addressBuilder.toString())
                }
                if (isAdded)
                    updateLocation()
            }
        }

    }

    private fun updateLocation() {
        val hashMap = HashMap<String, String>()
        hashMap["latitude"] = getFromDatabase(Constants.LATITUDE, "")
        hashMap["longitude"] = getFromDatabase(Constants.LONGITUDE, "")
        hashMap["locations"] = addressBuilder.toString()
        appViewModel.postApi(UPDATE_LAT_LNG, hashMap, requireContext(), true)
            .observe(viewLifecycleOwner, this)
    }

    override fun showProgress() {
//        Progress.show(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]
        if (isAdded)
            getLiveLocation(requireActivity())

        if (HomeActivity.latitude!=""){
            saveIntoDatabase(Constants.LATITUDE, HomeActivity.latitude)
            saveIntoDatabase(Constants.LONGITUDE, HomeActivity.longitude)
            updateLocation()
        }
        showBannerAdd()
        setProfile()
        hitGetRequests()
        clickListeners()
    }


    private fun setProfile() {
        homeBinding.apply {
            nameTextTV.text = "Hi, " + getFromDatabase(Constants.NAME, "")
            locTV.text = getFromDatabase(Constants.USER_LOCATION, "")
            if (getFromDatabase(Constants.USER_GENDER, "") == "0" )
                userImageIV.setImageDrawable(requireContext().getDrawable(R.drawable.gray_male))
            else
                userImageIV.setImageDrawable(requireContext().getDrawable(R.drawable.gray_female))
        }
    }

    private fun showBannerAdd() {
        // Create a new ad view.
        val adView = AdView(requireContext())

        adView.adUnitId = "ca-app-pub-3940256099942544/9214589741"
// Request an anchored adaptive banner with a width of 360.
        adView.setAdSize(
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                requireContext(),
                360
            )
        )
        this.adView = adView

        // Replace ad container with new ad view.
        homeBinding.adViewContainer.removeAllViews()
        homeBinding.adViewContainer.addView(adView)
        // Load the ad
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun setRequestsAdapter(body: List<EventBody>) {
        val requestsAdapter = RequestsAdapter(body, "home")
        homeBinding.requestsRV.adapter = requestsAdapter
        requestsAdapter.clickListener = { type, eventId ->
            when (type) {
                "base" -> {
                    val bundle = Bundle()
                    bundle.putString("eventId", eventId)
                    findNavController().navigate(R.id.detailsFragment, bundle)
                }

                "accept" -> {
                    hitAcceptReject(1, eventId)
                }

                "reject" -> {
                    hitAcceptReject(2, eventId)
                }
            }
        }
        requestsAdapter.notifyDataSetChanged()
    }

    private fun hitAcceptReject(status: Int, eventId: String) {
        val hashMap = HashMap<String, String>()
        hashMap["status"] = status.toString()
        hashMap["eventId"] = eventId
        appViewModel.postApi(ACCEPT_REJECT_REQUEST, hashMap, requireContext(), true)
            .observe(viewLifecycleOwner, this)
    }

    private fun hitGetRequests() {
        appViewModel.getApi(Constants.REQUESTS_LIST, requireContext(), true)
            .observe(viewLifecycleOwner, this)
        appViewModel.getApi(Constants.BANNERS_LIST, requireContext(), true)
            .observe(viewLifecycleOwner, this)
    }

    private fun clickListeners() {
        homeBinding.apply {
            userImageIV.setOnClickListener {
                findNavController().navigate(R.id.profileFragment)
            }
            addBT.setOnClickListener {
                findNavController().navigate(R.id.createRequestFragment)
            }
            notificationsLayout.setOnClickListener {
                findNavController().navigate(R.id.notificationsFragment)
            }
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(ContentValues.TAG, "askNotificationPermission: .. has permission")
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                Log.d(ContentValues.TAG, "askNotificationPermission: .. show rationale")
                Log.e(ContentValues.TAG, "Permissions Denied")
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    R.string.notification_rationale,
                    Snackbar.LENGTH_LONG
                )
                    .setAction(R.string.ok) {
                        // Request permission
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                            102
                        )
                    }.show()
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }


    override fun onChanged(value: Resource<JsonElement>) {
        when (value.status) {
            Status.SUCCESS -> {
                if (value.apiEndpoint == Constants.REQUESTS_LIST) {
                    val result = Gson().fromJson(
                        (value.data as JsonElement).toString(),
                        RequestListResponse::class.java
                    )
                    if (result.body.isEmpty()) {
                        homeBinding.noDataTV.visible()
                        homeBinding.requestsRV.gone()
                    } else {
                        homeBinding.noDataTV.gone()
                        homeBinding.requestsRV.visible()
                        setRequestsAdapter(result.body)
                    }
                }
                if (value.apiEndpoint == Constants.BANNERS_LIST) {
                    val result = Gson().fromJson(
                        (value.data as JsonElement).toString(),
                        BannersListResponse::class.java
                    )
                    setBannersAdapter(result.body)
                    askNotificationPermission()
                }
                if (value.apiEndpoint == ACCEPT_REJECT_REQUEST) {
                    val result = Gson().fromJson(
                        (value.data as JsonElement).toString(),
                        CommonResponse::class.java
                    )
                    showSuccessAlert(result.message.toString())
                    appViewModel.getApi(Constants.REQUESTS_LIST, requireContext(), true)
                        .observe(viewLifecycleOwner, this)

                }
                if (value.apiEndpoint == UPDATE_LAT_LNG) {
                    val result = Gson().fromJson(
                        (value.data as JsonElement).toString(),
                        CommonResponse::class.java
                    )
                }
            }

            Status.ERROR -> {
                showErrorAlert(value.message.toString())
            }

            else -> {

            }
        }
    }

    private fun setBannersAdapter(dataList: List<BannerItem>) {
        val viewHolder by lazy { BannerViewPager(requireContext(), dataList) }
        homeBinding?.viewPager?.adapter = viewHolder
        homeBinding.indicatorView.setupWithViewPager(homeBinding.viewPager)
        homeBinding.indicatorView.apply {
            setSliderColor(Color.parseColor("#80FFFFFF"), Color.parseColor("#FFFFFFFF"))
            setSliderWidth(resources.getDimension(com.intuit.sdp.R.dimen._6sdp))
            setSliderHeight(resources.getDimension(com.intuit.sdp.R.dimen._6sdp))
            setSlideMode(IndicatorSlideMode.SMOOTH)
            setIndicatorStyle(IndicatorStyle.CIRCLE)
            setPageSize(homeBinding.viewPager.adapter!!.itemCount)
            notifyDataChanged()
        }
    }

    override fun onPause() {
        adView?.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        adView?.resume()
    }

    override fun onDestroy() {
        adView?.destroy()
        super.onDestroy()
    }


}