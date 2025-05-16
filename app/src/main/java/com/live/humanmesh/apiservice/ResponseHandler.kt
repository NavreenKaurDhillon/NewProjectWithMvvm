package com.app.lete.apiservice


import android.util.Log
import android.util.MalformedJsonException
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.live.humanmesh.apiservice.Resource
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

open class ResponseHandler @Inject constructor() {

    fun handleResponse(data: JsonElement,apiEndpoint:String): Resource<JsonElement> {
        return Resource.success(data, apiEndpoint)
    }

    fun handleException(e: Exception,apiEndpoint:String): Resource<JsonElement> {
        return when (e) {
            is HttpException -> Resource.error(getErrorMessage(e), null, apiEndpoint)
            is MalformedJsonException -> Resource.error(
                getErrorMessage(46456, e),
                null,
                apiEndpoint
            )
            is IOException -> Resource.error(getErrorMessage(403, e), null, apiEndpoint)
            is JsonSyntaxException -> Resource.error(
                getErrorMessage(0, e),
                null,
                apiEndpoint
            )
            else -> Resource.error(getErrorMessage(Int.MAX_VALUE, e), null, apiEndpoint)
        }
    }

    fun errorMessage(errorBody:ResponseBody,apiEndpoint:String): Resource<JsonElement> {
        val errorBodyString = errorBody.string()

        return try {
            // Create a JsonParser instance
            val parser = JsonParser()
            // Attempt to parse the response body as JSON
            val jsonError = parser.parse(errorBodyString).asJsonObject
            Log.d("ewkbnjnwbe", "jsonError: $jsonError")
            val errorMessage = jsonError.getAsJsonPrimitive("message")?.asString
            Log.d("ewkbnjnwbe", "errorMessage: $errorMessage")

            if (errorMessage != null) {
                Resource.error(errorMessage, null, apiEndpoint)
            } else {
                Resource.error("Something went wrong !!", null, apiEndpoint)
            }
        } catch (e: JsonSyntaxException) {
            // Handle the case where the response body is not valid JSON
            Resource.error("Invalid response from server", null, apiEndpoint)
        } catch (e: IllegalStateException) {
            // Handle the case where the response body is not valid JSON
            Resource.error("Invalid response from server", null, apiEndpoint)
        }
    }

    private fun getErrorMessage(e: HttpException): String {
        return getErrorMsg(e.response()?.errorBody()!!).message
    }

    private fun getErrorMessage(code: Int, e: Exception): String {
        return when (code) {
            404 -> "Not Found"
            403 -> "Server Error"
            else -> e.toString()
        }
    }

    private fun getErrorMsg(responseBody: ResponseBody): BaseError {
        return try {
            val jsonObject = JSONObject(responseBody.string())
            BaseError(400, jsonObject.getString("message"))
        } catch (e: Exception) {
            BaseError(400, e.message!!)
        }
    }


}
