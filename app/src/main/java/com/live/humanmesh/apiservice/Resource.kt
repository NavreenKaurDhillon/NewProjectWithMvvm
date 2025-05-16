package com.live.humanmesh.apiservice

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import com.live.humanmesh.utils.Progress

data class Resource<out T>(val status: Status, val data: T?, val message: String?, var apiEndpoint:String="") {
    companion object {
        fun <T> success(data: T?,apiEndpoint:String): Resource<T> {
            Progress.hide()
            return Resource(Status.SUCCESS, data, null,apiEndpoint)
        }

        fun <T> error(msg: String, data: T?,apiEndpoint:String): Resource<T> {
            Progress.hide()
            return Resource(Status.ERROR, data, msg.replaceFirstChar { if (it.isLowerCase()) it.titlecase(
                Locale.ROOT) else it.toString() },apiEndpoint)
        }

        fun <T> loading(data: T?,context:Context,showLoader:Boolean=true): Resource<T> {
            CoroutineScope(Dispatchers.Main).launch{
                if (showLoader) {
                    Progress.show(context)
                }
            }
            return Resource(Status.LOADING, data, null)

        }fun <T> noInternet(data: T?): Resource<T> {
            Progress.hide()
            return Resource(Status.NO_INTERNET, data, null)
        }
    }
}

enum class Status {
    SUCCESS, ERROR, LOADING ,NO_INTERNET
}