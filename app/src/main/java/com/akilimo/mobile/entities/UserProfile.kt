package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_profiles",
    indices = [Index(value = ["user_name"], unique = true)]
)
data class UserProfile(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "profile_id")
    var profileId: Int? = null,

    @ColumnInfo(name = "device_token")
    var deviceToken: String? = null,

    @ColumnInfo(name = "user_name")
    var userName: String? = null,

    @ColumnInfo(name = "first_name")
    var firstName: String? = null,

    @ColumnInfo(name = "last_name")
    var lastName: String? = null,

    @ColumnInfo(name = "email")
    var email: String? = null,

    @ColumnInfo(name = "mobile_code")
    var mobileCode: String? = null,

    @ColumnInfo(name = "full_mobile_number")
    var fullMobileNumber: String? = null,

    @ColumnInfo(name = "farm_name")
    var farmName: String? = null,

    @ColumnInfo(name = "field_description")
    var fieldDescription: String? = null,

    @ColumnInfo(name = "gender")
    var gender: String? = null,

    @ColumnInfo(name = "akilimo_interest")
    var akilimoInterest: String? = null,

    @ColumnInfo(name = "selected_gender_index")
    var selectedGenderIndex: Int = -1,

    @ColumnInfo(name = "selected_interest_index")
    var selectedInterestIndex: Int = -1,

    @ColumnInfo(name = "send_email")
    var sendEmail: Boolean = false,

    @ColumnInfo(name = "send_sms")
    var sendSms: Boolean = false,

    @ColumnInfo(name = "language")
    var language: String? = null,

    @ColumnInfo(name = "country_code")
    var countryCode: String? = null,

    @ColumnInfo(name = "country_name")
    var countryName: String? = null,

    @ColumnInfo(name = "selected_country_index")
    var selectedCountryIndex: Int = 0,

    @ColumnInfo(name = "currency_code")
    var currencyCode: String? = null,

    @ColumnInfo(name = "risk_att")
    var riskAtt: Int = 0,

    @ColumnInfo(name = "selected_risk_index")
    var selectedRiskIndex: Int = 0
) {
    fun names(): String = "$firstName $lastName"
}
