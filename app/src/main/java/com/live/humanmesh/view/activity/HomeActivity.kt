package com.live.humanmesh.view.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.live.humanmesh.R
import com.live.humanmesh.apiservice.Resource
import com.live.humanmesh.apiservice.Status
import com.live.humanmesh.database.AppSharedPreferences
import com.live.humanmesh.database.AppSharedPreferences.getFromDatabase
import com.live.humanmesh.databinding.ActivityMainBinding
import com.live.humanmesh.model.CommonResponse
import com.live.humanmesh.model.RequestListResponse
import com.live.humanmesh.utils.AppUtils.gone
import com.live.humanmesh.utils.AppUtils.visible
import com.live.humanmesh.utils.Constants
import com.live.humanmesh.utils.Constants.UPDATE_LAT_LNG
import com.live.humanmesh.utils.location.LocationUpdateService
import com.live.humanmesh.utils.showErrorAlert
import com.live.humanmesh.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeActivity : AppCompatActivity() , Observer<Resource<JsonElement>>{

    private var binding: ActivityMainBinding? = null
    private lateinit var navController: NavController
    private lateinit var backPressedCallback: OnBackPressedCallback
    private lateinit var appViewModel: AppViewModel

    private var isBackPressed = 0
    companion object{
        var latitude =""
        var longitude =""
    }
/*
    val locationUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val latitude = intent.getDoubleExtra("latitude", 0.0)
            val longitude = intent.getDoubleExtra("longitude", 0.0)
            val address = intent.getStringExtra("address")
            if (latitude != 0.0 && longitude != 0.0) {
                // Handle the received location data
                Log.d("wekjfbjkbwef", "Received location: $latitude, $longitude")
                updateLocation(latitude, longitude, address)
            }
        }

        private fun updateLocation(latitude: Double, longitude: Double, address: String?) {
            val hashMap = HashMap<String, String>()
            hashMap["latitude"] = getFromDatabase(Constants.LATITUDE, "")
            hashMap["longitude"] = getFromDatabase(Constants.LONGITUDE, "")
            hashMap["locations"] = address.toString()
            appViewModel.postApi(UPDATE_LAT_LNG, hashMap,this@HomeActivity, true)
                .observe(this@HomeActivity, this@HomeActivity)
        }
    }
*/



    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setContentView(binding!!.root)
        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]

//      enableEdgeToEdge()
        AppSharedPreferences.saveIntoDatabase(Constants.IS_LOGIN, true)
        if (intent.extras!=null){
            if (intent.extras?.containsKey(Constants.LATITUDE) == true){
                Log.d("wekjfbjkbwef", "onCreate:  homeeeeeeeeeeee")
                latitude = intent.extras?.getString(Constants.LATITUDE).toString()
                longitude = intent.extras?.getString(Constants.LONGITUDE).toString()
            }
        }
       /* // Register the receiver dynamically
        val filter = IntentFilter("com.live.humanmesh.LOCATION_UPDATE")
        registerReceiver(locationUpdateReceiver , filter, Context.RECEIVER_EXPORTED)
        */
        navigationSetup()
        handleNotifications()

       /* val intent = Intent(this, LocationUpdateService::class.java).apply {
            action = "com.example.action.FOREGROUND_SERVICE"
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent) // Required for Android 8+
        } else {
            startService(intent)
        }*/

        clickListeners()

        backPressedCallback = object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d("weflkhkjwef", "handleOnBackPressed: ${navController.currentDestination?.id}")
                when (navController.currentDestination?.id) {
                    R.id.createRequestFragment, R.id.matchedUserFragment, R.id.chatFragment, R.id.profileFragment, R.id.settingsFragment -> {
                        navController.navigate(R.id.homeFragment)
                    }

                    R.id.userChatFragment -> {
                        if (!navController.popBackStack()) {
                            navController.navigate(R.id.chatFragment)
                        }
                    }

                     R.id.accountPrivacyFragment, R.id.subscriptionFragment, R.id.rewardsFragment, R.id.privacyPolicyFragment, R.id.faqFragment, R.id.contactUsFragment -> {
                        navController.navigate(R.id.settingsFragment)
                    }


                    R.id.detailsFragment -> {
                        if (!navController.popBackStack()) {
                            navController.navigate(R.id.homeFragment)
                        }
                    }
                    R.id.notificationsFragment -> {
                        if (!navController.popBackStack()) {
                            navController.navigate(R.id.homeFragment)
                        }
                    }
                    R.id.myEventsFragment -> {
                        if (!navController.popBackStack()) {
                            navController.navigate(R.id.profileFragment)
                        }
                    }

                    R.id.homeFragment -> {
                        if (isBackPressed == 0)
                            isBackPressed = 1
                        else
                            finishAffinity()
                    }

                    else -> {
                        if (!navController.popBackStack()) {
                            navController.navigate(R.id.homeFragment)
                        }
                    }
                }
            }
        }
        this.onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    private fun handleNotifications() {
        if (intent.extras?.getString("type") != null && intent.extras?.containsKey("type") == true) {
            val type = intent.extras?.getString("type")
            when (type) {
                "1" -> {
//                    navController.navigate(R.id.notificationsFragment)
                    navController.navigate(
                        R.id.notificationsFragment,
                        null,
                        navOptions {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    )
                }

                "2" -> {
                    val bundle = Bundle()
                    if (intent.getStringExtra("senderId") != null)
                        bundle.putString("receiverId", intent.getStringExtra("senderId"))
                    navController.navigate(
                        R.id.userChatFragment,
                        bundle,
                        navOptions {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    )
                }

                "3" -> {
                    val bundle = Bundle()
                    if (intent.getStringExtra("receiverId") != null)
                        bundle.putString("receiverId", intent.getStringExtra("receiverId"))
                    navController.navigate(
                        R.id.userChatFragment,
                        bundle,
                        navOptions {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    )                }

                "4" -> {
                    val bundle = Bundle()
                    if (intent.getStringExtra("eventId") != null)
                    bundle.putString("eventId",intent.getStringExtra("eventId") )
                    navController.navigate(
                        R.id.detailsFragment,
                        bundle,
                        navOptions {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    )                }

                "5" -> {
                    navController.navigate(
                        R.id.matchedUserFragment,
                        null,
                        navOptions {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    )
                    setSelected(1)
                }
            }
        }
    }

    private fun clickListeners() {
        binding?.apply {
            homeLayout.setOnClickListener {
                navController.navigate(R.id.homeFragment)
            }
            matchedUserLayout.setOnClickListener {
                navController.navigate(R.id.matchedUserFragment)
            }
            chatLayout.setOnClickListener {
                navController.navigate(R.id.chatFragment)
            }
            profileLayout.setOnClickListener {
                navController.navigate(R.id.profileFragment)
            }
            settingsLayout.setOnClickListener {
                navController.navigate(R.id.settingsFragment)
            }
        }
    }

    private fun navigationSetup() {
        val navHost = supportFragmentManager.findFragmentById(R.id.containerView) as NavHostFragment
        navController = navHost.navController
        setSelected(0)
        if (navController != null) {
            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.homeFragment -> {
                        setSelected(0)
                        showBottomNav()
                    }

                    R.id.matchedUserFragment -> {
                        setSelected(1)
                        showBottomNav()
                    }

                    R.id.chatFragment -> {
                        setSelected(2)
                        showBottomNav()
                    }

                    R.id.profileFragment -> {
                        setSelected(3)
                        showBottomNav()
                    }

                    R.id.settingsFragment -> {
                        setSelected(4)
                        showBottomNav()
                    }

                    else -> hideBottomNav()
                }
            }
        }
    }

    private fun showBottomNav() {
        binding?.bottonNavLayout?.visible()
    }

    private fun hideBottomNav() {
        binding?.bottonNavLayout?.gone()
    }

    private fun setSelected(pos: Int) {
        val tabs = listOf(
            Pair(binding?.homeIcon, binding?.homeText),
            Pair(binding?.matchedUserIcon, binding?.matchedUserText),
            Pair(binding?.chatIcon, binding?.chatText),
            Pair(binding?.profileIcon, binding?.profileText),
            Pair(binding?.settingsIcon, binding?.settingsText)
        )
        val icons = listOf(
            Pair(R.drawable.home_icon, R.drawable.home_icon_selected),
            Pair(R.drawable.matched_user_con, R.drawable.matched_user_icon_selected),
            Pair(R.drawable.chat_icon, R.drawable.chat_icon_selected),
            Pair(R.drawable.profile_icon, R.drawable.profile_icon_selected),
            Pair(R.drawable.settings_icon, R.drawable.settings_icon_selected)
        )
        tabs.forEachIndexed { index, (icon, text) ->
            if (index == pos)
                updateTab(icon, icons[index].second, text, true)
            else
                updateTab(icon, icons[index].first, text, false)
        }
    }

    private fun updateTab(imageView: ImageView?, drawable: Int, textView: TextView?, b: Boolean) {
        imageView?.setImageDrawable(ResourcesCompat.getDrawable(resources, drawable, null))
        if (b) {
            textView?.visible()
            imageView?.setColorFilter(
                resources.getColor(
                    R.color.app_color,
                    null
                )
            ) // Change to selected color            textView?.visible()
        } else {
            imageView?.setColorFilter(
                resources.getColor(
                    R.color.black_light,
                    null
                )
            ) // Change to selected colo
            textView?.gone()
        }
    }
    override fun onChanged(value: Resource<JsonElement>) {
        when (value.status) {
            Status.SUCCESS -> {
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


}