package com.app.lete.apiservice


import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Query
import retrofit2.http.Url


interface RetrofitInterface {
    @GET
    fun getApi(@Url url: String): Call<JsonElement>

    @GET
    fun getApiWithQuery(@Url url: String, @Query("postTypeId") type:String): Call<JsonElement>
    @GET
    fun searchApiWithQuery(@Url url: String, @Query("search") type:String): Call<JsonElement>

    @GET
    fun getGoogleApi(@Url url: String, @Query("type") type:String): Call<JsonElement>

    @GET
    fun getApiWithHeader( @Url url: String,
        @Header("authorization") token: String
    ): Call<JsonElement>

    @POST
    @FormUrlEncoded
    fun postWithParams(@Url url: String, @FieldMap obj: HashMap<String, String>): Call<JsonElement>

    @DELETE
    fun deleteApi(@Url url: String): Call<JsonElement>

    @PUT
    @FormUrlEncoded
    fun putWithParam(@Url url: String, @FieldMap obj: HashMap<String, String>): Call<JsonElement>

    @POST
    fun postApiWithHeader(
        @Url url: String,
        @Body obj: JsonObject,
        @Header("authorization") token: String
    ): Call<JsonElement>

    @POST
    @Multipart
    fun postWithMultipart(
        @Url url: String,
        @PartMap map: MutableMap<String, RequestBody>,
        @Part file: MultipartBody.Part?
    ): Call<JsonElement>


    @POST
    @Multipart
    fun postMultipart(
        @Url url: String,
        @Part file: MultipartBody.Part?
    ): Call<JsonElement>


    @POST
    @Multipart
    fun postWithMultipartMapImage(
        @Url url: String,
        @PartMap map: MutableMap<String, RequestBody>,
        @Part file: MultipartBody.Part?,
        @Part file2: MultipartBody.Part?
    ): Call<JsonElement>



    @POST
    @Multipart
    fun addClubWithMultipart(
        @Url url: String,
        @PartMap map: MutableMap<String, RequestBody>,
        @Part image: MultipartBody.Part?
    ): Call<JsonElement>

    @GET
    fun getClubType(
        @Url url: String
    ): Call<JsonElement>

    @GET
    fun getClubList(
        @Url url: String
    ): Call<JsonElement>

    @PUT
    @Multipart
    fun putWithMultipart(
        @Url url: String,
        @PartMap map: MutableMap<String, RequestBody>,
        @Part file: MultipartBody.Part?
    ): Call<JsonElement>

    @POST
    @Multipart
    fun postWithListMultipart(
        @Url url: String,
        @PartMap map: MutableMap<String, RequestBody>,
        @Part file: ArrayList<MultipartBody.Part?>,
        @Part list: ArrayList<MultipartBody.Part?>,
        @Part thumbnail: ArrayList<MultipartBody.Part?>
    ): Call<JsonElement>
    @POST
    @Multipart
    fun postWithListMultipart1(
        @Url url: String,
        @PartMap map: MutableMap<String, RequestBody>,
        @Part file: ArrayList<MultipartBody.Part?>
    ): Call<JsonElement>
    @POST
    @Multipart
    fun postWithListMultipartCertificate(
        @Url url: String,
        @PartMap map: MutableMap<String, RequestBody>,
        @Part file: ArrayList<MultipartBody.Part?>,
        @Part list: ArrayList<MultipartBody.Part?>,
        @Part thumbnail: ArrayList<MultipartBody.Part?>,
        @Part certificate: MultipartBody.Part?,
        @Part certificateThumbnail: MultipartBody.Part?
    ): Call<JsonElement>

    @POST
    @Multipart
    fun postWithListMultipartCertificate1(
        @Url url: String,
        @PartMap map: MutableMap<String, RequestBody>,
        @Part file: ArrayList<MultipartBody.Part?>,
        @Part certificate: MultipartBody.Part?,
        @Part certificateThumbnail: MultipartBody.Part?
    ): Call<JsonElement>

    @POST
    @Multipart
    fun postWithListMultipart(
        @Url url: String,
        @PartMap map: MutableMap<String, RequestBody>,
        @Part list: ArrayList<MultipartBody.Part>
    ): Call<JsonElement>
    @POST
    @Multipart
    fun postWithListMultipartWithoutImage(
        @Url url: String,
        @PartMap map: MutableMap<String, RequestBody>
    ): Call<JsonElement>

    @POST
    @Multipart
    fun postWithSingleImage(@Url url: String, @Part file: MultipartBody.Part): Call<JsonElement>
    @POST
    @Multipart
    fun postWithSingleImageThumnail(@Url url: String, @Part file: MultipartBody.Part ,@Part certificateThumbnail:MultipartBody.Part): Call<JsonElement>

    @POST
    @Multipart
    fun postWithListMultipartHeader(
        @Url url: String,
        @PartMap map: MutableMap<String, RequestBody>,
        @Part file: MultipartBody.Part?,
        @Part list: ArrayList<MultipartBody.Part>,
        @Header("authorization") token: String
    ): Call<JsonElement>

    @PUT
    @Multipart
    fun putWithListMultipart(
        @Url url: String,
        @PartMap map: MutableMap<String, RequestBody>,
        @Part file: MultipartBody.Part,
        @Part list: ArrayList<MultipartBody.Part>
    ): Call<JsonElement>

    @DELETE
    fun delete(@Url url: String): Call<JsonElement>

}
