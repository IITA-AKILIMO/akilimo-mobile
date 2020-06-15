package com.iita.akilimo.entities

import com.orm.SugarRecord

class ProfileInfo : SugarRecord<ProfileInfo>() {
     var id: Long = 0
     var deviceID: String? = null
     var userName: String? = null
     var firstName: String? = null
     var lastName: String? = null
     var email: String? = null
     var mobileCode: String? = null
     var fullMobileNumber: String? = null
     var farmName: String? = null
     var fieldDescription: String? = null
     var gender: String? = null
     var selectedGenderIndex = 0
     var sendEmail = false
     var sendSms = false

    fun getNames(): String {
        return String.format("%s %s", firstName, lastName)
    }
}