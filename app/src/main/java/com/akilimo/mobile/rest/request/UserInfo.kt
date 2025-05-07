package com.akilimo.mobile.rest.request

import com.fasterxml.jackson.annotation.JsonProperty

data class UserInfo(
    @JsonProperty("device_token")
    var deviceToken: String,

    @JsonProperty("phone_number")
    var phoneNumber: String = "NA",

    @JsonProperty("user_name")
    var userName: String = "akilimo",

    @JsonProperty("first_name")
    var firstName: String = "akilimo",

    @JsonProperty("last_name")
    var lastName: String = "user",

    @JsonProperty("gender")
    var gender: String = "NA",

    @JsonProperty("email_address")
    var emailAddress: String = "na@mail.com",

    @JsonProperty("farm_name")
    var farmName: String = "my_farm",

    @JsonProperty("send_sms")
    var sendSms: Boolean = false,

    @JsonProperty("send_email")
    var sendEmail: Boolean = false
)