package com.live.humanmesh.view.activity

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.live.humanmesh.R
import com.live.humanmesh.database.AppSharedPreferences
import com.live.humanmesh.databinding.ActivityAuthBinding
import com.live.humanmesh.utils.Constants
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAuthBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpNavigation()
    }

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

    private fun setUpNavigation() {
        val navHost = supportFragmentManager.findFragmentById(R.id.containerView) as NavHostFragment
        navController = navHost.navController
        Log.d("efmnwewkjfw", "setUpNavigation: ${AppSharedPreferences.getFromDatabase(Constants.IS_TUTORIAL_SHOWN, false)}")
        if (AppSharedPreferences.getTutorialFromDatabase(Constants.IS_TUTORIAL_SHOWN, false) == true) {
            navController.navigate(R.id.selectLanguageFragment)
        }
        else {
            navController.navigate(R.id.walkthroughFragment)
        }
    }
}