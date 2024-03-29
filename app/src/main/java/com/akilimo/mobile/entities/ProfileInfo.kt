package com.akilimo.mobile.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(tableName = "profile_info", indices = [Index(value = ["userName"], unique = true)])
open class ProfileInfo {

    @PrimaryKey(autoGenerate = true)
    var profileId: Int? = null
    var deviceToken: String? = null

    var userName: String? = null

    var firstName: String? = null

    var lastName: String? = null
    var email: String? = null
    var mobileCode: String? = null
    var fullMobileNumber: String? = null
    var farmName: String? = null
    var fieldDescription: String? = null

    var gender: String? = null
    var akilimoInterest: String? = null

    var selectedGenderIndex: Int = -1
    var selectedInterestIndex: Int = -1
    var sendEmail: Boolean = false
    var sendSms: Boolean = false

    var language: String? = null
    var countryCode: String? = null
    var countryName: String? = null
    var selectedCountryIndex: Int = 0
    var currency: String? = null

    var riskAtt: Int = 0
    var selectedRiskIndex: Int = 0

    fun getNames(): String {
        return String.format("%s %s", firstName, lastName)
    }
}
