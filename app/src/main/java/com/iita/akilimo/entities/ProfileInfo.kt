package com.iita.akilimo.entities

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

@DatabaseTable
open class ProfileInfo {


    @DatabaseField(columnName = "id", generatedId = true)
    var profileId: Int? = null
    var deviceToken: String? = null
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

    var selectedGenderIndex: Int = 0
    var sendEmail: Boolean = false
    var sendSms: Boolean = false

    fun getNames(): String {
        return String.format("%s %s", firstName, lastName)
    }
}