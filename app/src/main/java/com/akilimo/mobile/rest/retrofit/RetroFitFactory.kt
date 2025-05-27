package com.akilimo.mobile.rest.retrofit

import com.akilimo.mobile.BuildConfig
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit

object RetroFitFactory {
    fun create(baseUrl: String, timeoutSeconds: Long = 30): Retrofit {

        val builder = OkHttpClient.Builder()
            .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.networkInterceptors().add(httpLoggingInterceptor)
        }

        val client = builder.build()

        val objectMapper = JsonMapper.builder()
            .addModule(kotlinModule()) // Enable Kotlin class support
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY) // Keep declared field order
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .build()

    }
}
