package com.live.humanmesh.repository

import com.app.lete.apiservice.ResponseHandler
import com.app.lete.apiservice.RetrofitInterface
import com.google.gson.JsonElement
import com.live.humanmesh.apiservice.Resource
import com.live.humanmesh.utils.Constants.BASE_URL
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.resume
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AppRepository @Inject constructor(private val apiService: RetrofitInterface, private var responseHandler: ResponseHandler) {

    suspend fun postWithMultipart(apiEndpoint: String, hm: HashMap<String, RequestBody>, file: MultipartBody.Part? ): Resource<JsonElement> {
        return suspendCancellableCoroutine { continuation ->
            val call: Call<JsonElement> = apiService.postWithMultipart(BASE_URL + apiEndpoint,hm,file)
            call.enqueue(object : Callback<JsonElement> {
                override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>)
                {
                    if (response.isSuccessful) {
                        continuation.resume(responseHandler.handleResponse(response.body()!!, apiEndpoint))
                    } else {
                        val errorBody: ResponseBody? = response.errorBody()
                        if (errorBody != null) {
                            continuation.resume(responseHandler.errorMessage(errorBody, apiEndpoint))
                        } else {
                            continuation.resume(responseHandler.handleException(Exception("Network error: ${response.code()} ${response.message()}"), apiEndpoint))
                        }
                    }
                }
                override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                    when (t) {
                        is ArithmeticException -> {
                            continuation.resume(responseHandler.handleException(t, apiEndpoint))
                        }
                        is IOException -> {
                            continuation.resume(responseHandler.handleException(t, apiEndpoint))
                        }
                        is Exception -> {
                            continuation.resume(responseHandler.handleException(t, apiEndpoint))
                        }
                    }
                }
            })
            continuation.invokeOnCancellation {
                call.cancel()
            }
        }
    }
    suspend fun postMultipart(apiEndpoint: String, file: MultipartBody.Part? ): Resource<JsonElement> {
        return suspendCancellableCoroutine { continuation ->
            val call: Call<JsonElement> = apiService.postMultipart(BASE_URL + apiEndpoint,file)
            call.enqueue(object : Callback<JsonElement> {
                override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>)
                {
                    if (response.isSuccessful) {
                        continuation.resume(responseHandler.handleResponse(response.body()!!, apiEndpoint))
                    } else {
                        val errorBody: ResponseBody? = response.errorBody()
                        if (errorBody != null) {
                            continuation.resume(responseHandler.errorMessage(errorBody, apiEndpoint))
                        } else {
                            continuation.resume(responseHandler.handleException(Exception("Network error: ${response.code()} ${response.message()}"), apiEndpoint))
                        }
                    }
                }
                override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                    when (t) {
                        is ArithmeticException -> {
                            continuation.resume(responseHandler.handleException(t, apiEndpoint))
                        }
                        is IOException -> {
                            continuation.resume(responseHandler.handleException(t, apiEndpoint))
                        }
                        is Exception -> {
                            continuation.resume(responseHandler.handleException(t, apiEndpoint))
                        }
                    }
                }
            })
            continuation.invokeOnCancellation {
                call.cancel()
            }
        }
    }

    suspend fun postApi(apiEndpoint: String, obj:HashMap<String,String>): Resource<JsonElement> {
        return suspendCancellableCoroutine { continuation ->
            val call: Call<JsonElement> = apiService.postWithParams(BASE_URL + apiEndpoint,obj)
            call.enqueue(object : Callback<JsonElement> {
                override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                    if (response.isSuccessful) {
                        continuation.resume(responseHandler.handleResponse(response.body()!!, apiEndpoint))
                    } else {
                        val errorBody: ResponseBody? = response.errorBody()
                        if (errorBody != null) {
                            continuation.resume(responseHandler.errorMessage(errorBody, apiEndpoint))
                        } else {
                            continuation.resume(responseHandler.handleException(Exception("Network error: ${response.code()} ${response.message()}"), apiEndpoint))
                        }
                    }
                }
                override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                    when (t) {
                        is ArithmeticException -> {
                            continuation.resume(responseHandler.handleException(t, apiEndpoint))
                        }
                        is IOException -> {
                            continuation.resume(responseHandler.handleException(t, apiEndpoint))
                        }
                        is Exception -> {
                            continuation.resume(responseHandler.handleException(t, apiEndpoint))
                        }
                    }
                }
            })
            continuation.invokeOnCancellation {
                call.cancel()
            }
        }
    }
    suspend fun deleteApi(apiEndpoint: String): Resource<JsonElement> {
        return suspendCancellableCoroutine { continuation ->
            val call: Call<JsonElement> = apiService.deleteApi(BASE_URL + apiEndpoint)
            call.enqueue(object : Callback<JsonElement> {
                override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                    if (response.isSuccessful) {
                        continuation.resume(responseHandler.handleResponse(response.body()!!, apiEndpoint))
                    } else {
                        val errorBody: ResponseBody? = response.errorBody()
                        if (errorBody != null) {
                            continuation.resume(responseHandler.errorMessage(errorBody, apiEndpoint))
                        } else {
                            continuation.resume(responseHandler.handleException(Exception("Network error: ${response.code()} ${response.message()}"), apiEndpoint))
                        }
                    }
                }
                override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                    when (t) {
                        is ArithmeticException -> {
                            continuation.resume(responseHandler.handleException(t, apiEndpoint))
                        }
                        is IOException -> {
                            continuation.resume(responseHandler.handleException(t, apiEndpoint))
                        }
                        is Exception -> {
                            continuation.resume(responseHandler.handleException(t, apiEndpoint))
                        }
                    }
                }
            })
            continuation.invokeOnCancellation {
                call.cancel()
            }
        }
    }
    suspend fun getApi(apiEndpoint: String): Resource<JsonElement> {
        return suspendCancellableCoroutine { continuation ->
            val call: Call<JsonElement> = apiService.getApi(BASE_URL + apiEndpoint)
            call.enqueue(object : Callback<JsonElement> {
                override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                    if (response.isSuccessful) {
                        continuation.resume(responseHandler.handleResponse(response.body()!!, apiEndpoint))
                    } else {
                        val errorBody: ResponseBody? = response.errorBody()
                        if (errorBody != null) {
                            continuation.resume(responseHandler.errorMessage(errorBody, apiEndpoint))
                        } else {
                            continuation.resume(responseHandler.handleException(Exception("Network error: ${response.code()} ${response.message()}"), apiEndpoint))
                        }
                    }
                }
                override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                    when (t) {
                        is ArithmeticException -> {
                            continuation.resume(responseHandler.handleException(t, apiEndpoint))
                        }
                        is IOException -> {
                            continuation.resume(responseHandler.handleException(t, apiEndpoint))
                        }
                        is Exception -> {
                            continuation.resume(responseHandler.handleException(t, apiEndpoint))
                        }
                    }
                }
            })
            continuation.invokeOnCancellation {
                call.cancel()
            }
        }
    }
    suspend fun putWithParam(apiEndpoint: String, obj:HashMap<String,String>): Resource<JsonElement> {
        return suspendCancellableCoroutine { continuation ->
            val call: Call<JsonElement> = apiService.putWithParam(BASE_URL + apiEndpoint,obj)
            call.enqueue(object : Callback<JsonElement> {
                override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                    if (response.isSuccessful) {
                        continuation.resume(responseHandler.handleResponse(response.body()!!, apiEndpoint))
                    } else {
                        val errorBody: ResponseBody? = response.errorBody()
                        if (errorBody != null) {
                            continuation.resume(responseHandler.errorMessage(errorBody, apiEndpoint))
                        } else {
                            continuation.resume(responseHandler.handleException(Exception("Network error: ${response.code()} ${response.message()}"), apiEndpoint))
                        }
                    }
                }
                override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                    when (t) {
                        is ArithmeticException -> {
                            continuation.resume(responseHandler.handleException(t, apiEndpoint))
                        }
                        is IOException -> {
                            continuation.resume(responseHandler.handleException(t, apiEndpoint))
                        }
                        is Exception -> {
                            continuation.resume(responseHandler.handleException(t, apiEndpoint))
                        }
                    }
                }
            })
            continuation.invokeOnCancellation {
                call.cancel()
            }
        }
    }


}