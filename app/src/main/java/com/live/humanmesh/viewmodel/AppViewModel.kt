package com.live.humanmesh.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.google.gson.JsonElement
import com.live.humanmesh.apiservice.Resource
import com.live.humanmesh.repository.AppRepository
import com.live.humanmesh.utils.checkNetworkConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject


@HiltViewModel
class AppViewModel  @Inject constructor(private var repository: AppRepository) : ViewModel(){

    fun postApi(apiEndPoint:String, hashMap: HashMap<String,String>, context: Context, showLoader:Boolean): LiveData<Resource<JsonElement>> {
        return liveData(Dispatchers.IO) {
            if(!checkNetworkConnection(context))
            {
                emit(Resource.noInternet(null))
                return@liveData
            }
            emit(Resource.loading(null,context,showLoader))
            val response = repository.postApi(apiEndPoint,hashMap)
            emit(response)
        }
    }
    fun getApi(apiEndPoint:String,context: Context, showLoader:Boolean): LiveData<Resource<JsonElement>> {
        return liveData(Dispatchers.IO) {
            if(!checkNetworkConnection(context))
            {
                emit(Resource.noInternet(null))
                return@liveData
            }
            emit(Resource.loading(null,context,showLoader))
            val response = repository.getApi(apiEndPoint)
            emit(response)
        }
    }
    fun postWithMultipart(apiEndPoint:String,hm: HashMap<String, RequestBody>,
                          context:Context,showLoader:Boolean,image: MultipartBody.Part?):LiveData<Resource<JsonElement>> {
        return liveData(Dispatchers.IO) {
            if(!checkNetworkConnection(context))
            {
                emit(Resource.noInternet(null))
                return@liveData
            }
            emit(Resource.loading(null,context,showLoader))
            val response = repository.postWithMultipart(apiEndPoint, hm, image)
            emit(response)
        }
    }
    fun postMultipart(apiEndPoint:String,
                          context:Context,showLoader:Boolean,image: MultipartBody.Part?):LiveData<Resource<JsonElement>> {
        return liveData(Dispatchers.IO) {
            if(!checkNetworkConnection(context))
            {
                emit(Resource.noInternet(null))
                return@liveData
            }
            emit(Resource.loading(null,context,showLoader))
            val response = repository.postMultipart(apiEndPoint, image)
            emit(response)
        }
    }
   /* fun getApi(apiEndPoint:String,context:Context,showLoader:Boolean): LiveData<Resource<JsonElement>> {
        return liveData(Dispatchers.IO) {
            if(!checkNetworkConnection(context))
            {
                emit(Resource.noInternet(null))
                return@liveData
            }
            emit(Resource.loading(null,context,showLoader))
            val response = repository.getApi(apiEndPoint)
            emit(response)
        }
    }*/

    fun deleteApi(apiEndPoint:String,context:Context,showLoader:Boolean):LiveData<Resource<JsonElement>> {
        return liveData(Dispatchers.IO) {
            if(!checkNetworkConnection(context))
            {
                emit(Resource.noInternet(null))
                return@liveData
            }
            emit(Resource.loading(null,context,showLoader))
            val response = repository.deleteApi(apiEndPoint)
            emit(response)
        }
    }

    fun putWithParam(apiEndPoint:String, hashMap: HashMap<String,String>,context:Context,showLoader:Boolean):LiveData<Resource<JsonElement>> {
        return liveData(Dispatchers.IO) {
            if(!checkNetworkConnection(context))
            {
                emit(Resource.noInternet(null))
                return@liveData
            }
            emit(Resource.loading(null,context,showLoader))
            val response = repository.putWithParam(apiEndPoint,hashMap)
            emit(response)
        }
    }

}