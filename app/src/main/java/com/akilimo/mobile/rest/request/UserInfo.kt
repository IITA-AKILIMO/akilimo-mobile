package com.akilimo.mobile.rest.request

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
class UserInfo {
    var deviceToken: String = "NA"
    var phoneNumber: String = "NA"
    var userName: String = "akilimo"
    var firstName: String = "AKILIMO"
    var lastName: String = "USER"
    var gender: String = "NA"
    var email: String = "NA"
    var fieldDescription: String = "NA"
    var sendSms: Boolean = false
    var sendEmail: Boolean = false
}
