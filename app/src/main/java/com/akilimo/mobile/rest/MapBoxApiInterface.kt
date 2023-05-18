package com.akilimo.mobile.rest

import com.akilimo.mobile.rest.response.ReverseGeoCode
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

//
//internal object APIClient {
//    private var retrofit: Retrofit? = null
//
//    fun getClient(): Retrofit? {
//        val interceptor = HttpLoggingInterceptor()
//        interceptor.level = HttpLoggingInterceptor.Level.BODY
//        val client: OkHttpClient = Builder().addInterceptor(interceptor).build()
//        retrofit = Retrofit.Builder()
//            .baseUrl("https://reqres.in")
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(client)
//            .build()
//        return retrofit
//    }
//}

object MapBoxApi {

    @JvmStatic
    fun create(): MapBoxApiInterface {

        val builder = OkHttpClient.Builder()
//            val httpLoggingInterceptor = HttpLoggingInterceptor()
//            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY;
//            builder.networkInterceptors().add(httpLoggingInterceptor);
        val client = builder.build()

        val retrofit = Retrofit.Builder().client(client)
            .addConverterFactory(JacksonConverterFactory.create())
            .baseUrl("https://api.mapbox.com").build()

        return retrofit.create(MapBoxApiInterface::class.java)
    }
}

interface MapBoxApiInterface {

    @GET("geocoding/v5/mapbox.places/{lon},{lat}.json")
    fun reverseGeoCode(
        @Path("lon") lon: Double,
        @Path("lat") lat: Double,
        @Query("types") types: String,
        @Query("access_token") accessToken: String
    ): Call<ReverseGeoCode>

}
