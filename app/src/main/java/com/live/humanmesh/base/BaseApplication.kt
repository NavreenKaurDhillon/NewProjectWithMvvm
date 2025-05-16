package com.live.humanmesh.base

import android.app.Application
import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.live.humanmesh.utils.socket.SocketManager
//import com.live.humanmesh.utils.socket.SocketManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@HiltAndroidApp
class BaseApplication : Application() {

    companion object{
        lateinit var appContext: Context
        var context : BaseApplication?=null
        var mSocketManager: SocketManager? = null
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        appContext = applicationContext
        FirebaseApp.initializeApp(applicationContext)
        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            // Initialize the Google Mobile Ads SDK on a background thread.
            MobileAds.initialize(applicationContext) {}
        }
    }
    fun getSocketMySocketManager(): SocketManager? {
        mSocketManager = if (mSocketManager == null) {
            SocketManager()
        } else {
            return mSocketManager
        }
        return mSocketManager
    }

}