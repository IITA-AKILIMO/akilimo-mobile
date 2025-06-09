package com.akilimo.mobile.rest.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserInfo(
    @Json(name = "device_token")
    var deviceToken: String,

    @Json(name = "phone_number")
    var phoneNumber: String = "NA",

    @Json(name = "user_name")
    var userName: String = "akilimo",

    @Json(name = "first_name")
    var firstName: String = "akilimo",

    @Json(name = "last_name")
    var lastName: String = "user",

    @Json(name = "gender")
    var gender: String = "NA",

    @Json(name = "email_address")
    var emailAddress: String = "na@mail.com",

    @Json(name = "farm_name")
    var farmName: String = "my_farm",

    @Json(name = "send_sms")
    var sendSms: Boolean = false,

    @Json(name = "send_email")
    var sendEmail: Boolean = false
)
