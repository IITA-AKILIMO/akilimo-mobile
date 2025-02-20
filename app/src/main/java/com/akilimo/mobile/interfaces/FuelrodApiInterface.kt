package com.akilimo.mobile.interfaces

import com.akilimo.mobile.data.RemoteConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path


interface FuelrodApiInterface {

    companion object {

        fun create(apiBaseUrl: String = "https://api.munywele.co.ke/"): FuelrodApiInterface {

            val builder = OkHttpClient.Builder()
//            val httpLoggingInterceptor = HttpLoggingInterceptor()
//            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
//            builder.networkInterceptors().add(httpLoggingInterceptor)
            val client = builder.build()

            val retrofit = Retrofit.Builder()
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl(apiBaseUrl)
                .build()

            return retrofit.create(FuelrodApiInterface::class.java)
        }
    }


    @GET("v1/remote-config/app-name/{app}")
    fun readConfig(@Path("app") app: String): Call<List<RemoteConfig>>
}
