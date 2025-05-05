package com.akilimo.mobile.rest.request

import com.fasterxml.jackson.annotation.JsonProperty

class UserInfo {

    @JsonProperty("device_token")
    var deviceToken: String = "NA"

    @JsonProperty("phone_number")
    var phoneNumber: String = "NA"

    @JsonProperty("user_name")
    var userName: String = "akilimo"

    @JsonProperty("first_name")
    var firstName: String = "AKILIMO"

    @JsonProperty("last_name")
    var lastName: String = "USER"

    @JsonProperty("gender")
    var gender: String = "NA"

    @JsonProperty("email")
    var email: String = "NA"

    @JsonProperty("field_description")
    var fieldDescription: String = "NA"

    @JsonProperty("send_sms")
    var sendSms: Boolean = false

    @JsonProperty("send_email")
    var sendEmail: Boolean = false
}