package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_profiles",
    indices = [Index(value = ["device_token"], unique = true)]
)
data class UserProfile(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "profile_id")
    var profileId: Int? = null,

    @ColumnInfo(name = "device_token")
    var deviceToken: String? = null,

    @ColumnInfo(name = "first_name")
    var firstName: String? = null,

    @ColumnInfo(name = "last_name")
    var lastName: String? = null,

    @ColumnInfo(name = "email")
    var email: String = "na@mail.com",

    @ColumnInfo(name = "mobile_code")
    var mobileCode: String = "",

    @ColumnInfo(name = "phone_number")
    var phoneNumber: String? = null,

    @ColumnInfo(name = "farm_name")
    var farmName: String = "",

    @ColumnInfo(name = "field_description")
    var fieldDescription: String = "AKILIMO Field",

    @ColumnInfo(name = "gender")
    var gender: String? = null,

    @ColumnInfo(name = "akilimo_interest")
    var akilimoInterest: String? = null,

    @ColumnInfo(name = "send_email")
    var sendEmail: Boolean = false,

    @ColumnInfo(name = "send_sms")
    var sendSms: Boolean = false,

    @ColumnInfo(name = "country_code")
    var countryCode: String = "",

    @ColumnInfo(name = "country_name")
    var countryName: String = "",

    @ColumnInfo(name = "currency_code")
    var currencyCode: String = "",

    @ColumnInfo(name = "risk_att")
    var riskAtt: Int = 0,
)
