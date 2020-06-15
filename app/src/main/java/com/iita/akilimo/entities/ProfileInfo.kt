package com.iita.akilimo.entities

import com.orm.SugarRecord
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class ProfileInfo : RealmObject() {

    @PrimaryKey
    var id: Long = 0
    var deviceID: String? = null
    var userName: String? = null

    @Required
    var firstName: String? = null

    @Required
    var lastName: String? = null
    var email: String? = null
    var mobileCode: String? = null
    var fullMobileNumber: String? = null
    var farmName: String? = null
    var fieldDescription: String? = null

    @Required
    var gender: String? = null

    var selectedGenderIndex:Int = 0
    var sendEmail:Boolean = false
    var sendSms:Boolean = false

    fun getNames(): String {
        return String.format("%s %s", firstName, lastName)
    }
}