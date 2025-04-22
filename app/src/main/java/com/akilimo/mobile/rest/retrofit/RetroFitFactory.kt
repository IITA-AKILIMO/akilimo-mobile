package com.akilimo.mobile.rest.retrofit

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

object RetroFitFactory {
    fun create(baseUrl: String): Retrofit {

        val builder = OkHttpClient.Builder()
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        builder.networkInterceptors().add(httpLoggingInterceptor)
        val client = builder.build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
//            .client(client)
            .addConverterFactory(JacksonConverterFactory.create())
            .build()

    }
}
