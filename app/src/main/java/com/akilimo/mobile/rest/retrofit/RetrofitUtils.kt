package com.akilimo.mobile.rest.retrofit

import com.akilimo.mobile.rest.response.ApiErrorResponse
import com.squareup.moshi.Moshi
import retrofit2.Response

fun <T> Response<T>.parseError(): ApiErrorResponse? {
    val errorBody = errorBody()?.string() ?: return null
    return try {
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(ApiErrorResponse::class.java)
        adapter.fromJson(errorBody)
    } catch (e: Exception) {
        null
    }
}