package com.app.lete.apiservice

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.widget.Toast

class CommonClass {
    fun checkNetworkConnection(ctx: Context) :Boolean {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true
        if (!isConnected) {
            return false
        }
        return true
    }

    fun alertErrorMessage(ctx: Context, message:String) {
        Toast.makeText(ctx,message, Toast.LENGTH_SHORT).show()
    }

}