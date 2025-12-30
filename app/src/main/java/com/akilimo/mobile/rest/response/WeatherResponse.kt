package com.akilimo.mobile.rest.response

import com.squareup.moshi.Json

data class WeatherResponse(
    @param:Json(name = "location")
    val location: Location = Location(),

    @param:Json(name = "current")
    val current: Current = Current()
)

data class Location(
    @param:Json(name = "name")
    val name: String = "",

    @param:Json(name = "region")
    val region: String = "",

    @param:Json(name = "country")
    val country: String = "",

    @param:Json(name = "lat")
    val lat: Double = 0.0,

    @param:Json(name = "lon")
    val lon: Double = 0.0,

    @param:Json(name = "localtime")
    val localtime: String = ""
)

data class Current(
    @param:Json(name = "temp_c")
    val tempCelsius: Double = 0.0,

    @param:Json(name = "feelslike_c")
    val feelslikeCelsius: Double = 0.0,

    @param:Json(name = "humidity")
    val humidity: Int = 0,

    @param:Json(name = "wind_kph")
    val windKph: Double = 0.0,

    @param:Json(name = "condition")
    val condition: Condition = Condition()
)

data class Condition(

    @param:Json(name = "text")
    val text: String = ""
)
