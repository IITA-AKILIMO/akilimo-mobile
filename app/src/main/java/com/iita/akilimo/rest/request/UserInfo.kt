package com.iita.akilimo.rest.request

import com.fasterxml.jackson.annotation.JsonProperty
import lombok.Data

@Data
class UserInfo(

) {
    var deviceID: String = "NA"
    @JsonProperty("userPhoneCC")
    var mobileCountryCode: String = "NA"
    @JsonProperty("userPhoneNr")
    var mobileNumber: String = "NA"
    @JsonProperty("fullPhoneNumber")
    var fullPhoneNumber: String = "NA"
    @JsonProperty("userName")
    var firstName: String = "NA"
    @JsonProperty("lastName")
    var secondName: String = "NA"
    @JsonProperty("userEmail")
    var emailAddress: String = "NA"
    @JsonProperty("userField")
    var fieldDescription: String = "NA"

    @JsonProperty("SMS")
    var sendSms: Boolean = false
    @JsonProperty("email")
    var sendEmail: Boolean = false
}