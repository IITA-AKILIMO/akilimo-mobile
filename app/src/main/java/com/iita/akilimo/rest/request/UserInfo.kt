package com.iita.akilimo.rest.request

import com.fasterxml.jackson.annotation.JsonProperty
import lombok.Data

@Data
class UserInfo {
    var deviceID: String = "NA"
    var mobileCountryCode: String = "NA"
    var mobileNumber: String = "NA"
    var fullPhoneNumber: String = "NA"
    var userName: String = "NA"
    var firstName: String = "NA"
    var secondName: String = "NA"
    var emailAddress: String = "NA"
    var fieldDescription: String = "NA"
    var sendSms: Boolean = false
    var sendEmail: Boolean = false
}