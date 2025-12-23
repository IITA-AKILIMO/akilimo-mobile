package com.akilimo.mobile.rest.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserInfo(
    @param:Json(name = "device_token")
    var deviceToken: String,

    @param:Json(name = "phone_number")
    var phoneNumber: String,

    @param:Json(name = "user_name")
    var userName: String,

    @param:Json(name = "first_name")
    var firstName: String,

    @param:Json(name = "last_name")
    var lastName: String,

    @param:Json(name = "gender")
    var gender: String,

    @param:Json(name = "email_address")
    var emailAddress: String,

    @param:Json(name = "farm_name")
    var farmName: String,

    @param:Json(name = "send_sms")
    var sendSms: Boolean,

    @param:Json(name = "send_email")
    var sendEmail: Boolean,

    @param:Json(name = "risk_attitude")
    val riskAttitude: Int = 0
)
