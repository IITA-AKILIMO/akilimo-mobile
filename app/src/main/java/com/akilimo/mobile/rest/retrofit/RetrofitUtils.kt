package com.akilimo.mobile.rest.retrofit

import com.akilimo.mobile.rest.response.ApiErrorResponse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import retrofit2.Response

fun <T> Response<T>.parseError(): ApiErrorResponse? {
    val errorBody = errorBody()?.string() ?: return null
    return try {
        val mapper = jacksonObjectMapper()
        mapper.readValue(errorBody, ApiErrorResponse::class.java)
    } catch (e: Exception) {
        null
    }
}