package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.akilimo.mobile.enums.EnumAreaUnit
import com.akilimo.mobile.enums.EnumCountry

@Entity(tableName = "user_preferences")
data class UserPreferences(

    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int = 1,

    @ColumnInfo(name = "language_code")
    val languageCode: String = "en",

    @ColumnInfo(name = "first_name")
    val firstName: String? = null,

    @ColumnInfo(name = "last_name")
    val lastName: String? = null,

    @ColumnInfo(name = "email")
    val email: String? = null,

    @ColumnInfo(name = "phone_number")
    val phoneNumber: String? = null,

    @ColumnInfo(name = "phone_country_code")
    val phoneCountryCode: String? = null,

    @ColumnInfo(name = "gender")
    val gender: String? = null,

    @ColumnInfo(name = "country")
    val country: EnumCountry = EnumCountry.Unsupported,

    @ColumnInfo(name = "bio")
    val bio: String? = null,

    @ColumnInfo(name = "notify_by_email")
    val notifyByEmail: Boolean = false,

    @ColumnInfo(name = "notify_by_sms")
    val notifyBySms: Boolean = false,

    @ColumnInfo(name = "preferred_area_unit")
    val preferredAreaUnit: EnumAreaUnit = EnumAreaUnit.ACRE,

    @ColumnInfo(name = "dark_mode")
    val darkMode: Boolean = false
)
