package com.akilimo.mobile.rest.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserInfo(
    @param:Json(name = "device_token")
    var deviceToken: String,

    @param:Json(name = "phone_number")
    var phoneNumber: String = "NA",

    @param:Json(name = "user_name")
    var userName: String = "akilimo",

    @param:Json(name = "first_name")
    var firstName: String = "akilimo",

    @param:Json(name = "last_name")
    var lastName: String = "user",

    @param:Json(name = "gender")
    var gender: String = "NA",

    @param:Json(name = "email_address")
    var emailAddress: String = "na@mail.com",

    @param:Json(name = "farm_name")
    var farmName: String = "my_farm",

    @param:Json(name = "send_sms")
    var sendSms: Boolean = false,

    @param:Json(name = "send_email")
    var sendEmail: Boolean = false
)
