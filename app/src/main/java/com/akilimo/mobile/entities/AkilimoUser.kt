package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.akilimo.mobile.dto.OperationEntry
import com.akilimo.mobile.enums.EnumAdvice
import com.akilimo.mobile.enums.EnumAreaUnit
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.enums.EnumInvestmentPref
import java.time.LocalDate
import java.util.Locale

@Entity(
    tableName = "akilimo_users",
    indices = [Index(value = ["user_name"], unique = true)]
)
data class AkilimoUser(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int? = null,

    @ColumnInfo(name = "device_token")
    val deviceToken: String? = null,

    @ColumnInfo(name = "user_name")
    val userName: String,

    @ColumnInfo(name = "first_name")
    val firstName: String? = null,

    @ColumnInfo(name = "last_name")
    val lastName: String? = null,

    @ColumnInfo(name = "email")
    val email: String? = null,

    @ColumnInfo(name = "mobile_number")
    val mobileNumber: String? = null,

    @ColumnInfo(name = "mobile_country_code")
    val mobileCountryCode: String? = null,

    @ColumnInfo(name = "farm_name")
    val farmName: String? = null,

    @ColumnInfo(name = "farm_country")
    val enumCountry: EnumCountry = EnumCountry.Unsupported,

    @ColumnInfo(name = "farm_size_unit")
    val enumAreaUnit: EnumAreaUnit = EnumAreaUnit.ACRE,

    @ColumnInfo(name = "farm_size")
    val farmSize: Double = 1.0,

    @ColumnInfo(name = "custom_farm_size")
    val customFarmSize: Boolean? = null,

    @ColumnInfo(name = "farm_description")
    val farmDescription: String? = null,

    @ColumnInfo(name = "latitude")
    val latitude: Double = 0.0,

    @ColumnInfo(name = "longitude")
    val longitude: Double = 0.0,

    @ColumnInfo(name = "altitude")
    val altitude: Double = 0.0,

    @ColumnInfo(name = "zoom_level")
    val zoomLevel: Double = 0.0,

    @ColumnInfo(name = "gender")
    val gender: String? = null,

    @ColumnInfo(name = "akilimo_interest")
    val akilimoInterest: String? = null,

    @ColumnInfo(name = "send_email")
    val sendEmail: Boolean = false,

    @ColumnInfo(name = "send_sms")
    val sendSms: Boolean = false,

    @ColumnInfo(name = "language_code")
    val languageCode: String? = null,

    @ColumnInfo(name = "risk_att")
    val riskAtt: Int = 0,

    @ColumnInfo(name = "planting_date")
    val plantingDate: LocalDate? = null,

    @ColumnInfo(name = "harvest_date")
    val harvestDate: LocalDate? = null,

    @ColumnInfo(name = "planting_flex")
    val plantingFlex: Long = 0L,

    @ColumnInfo(name = "harvest_flex")
    val harvestFlex: Long = 0L,

    @ColumnInfo(name = "provided_alternative_date")
    val providedAlterNativeDate: Boolean = false,

    @ColumnInfo(name = "tillage_operations")
    val tillageOperations: List<OperationEntry> = emptyList(),

    @ColumnInfo(name = "investment_preferences")
    val investmentPref: EnumInvestmentPref? = null,

    @ColumnInfo(name = "active_use_case")
    val activeAdvise: EnumAdvice? = null
) {
    fun getNames(): String {
        val f = firstName?.trim().orEmpty()
        val l = lastName?.trim().orEmpty()
        return when {
            f.isNotEmpty() && l.isNotEmpty() -> String.format(Locale.getDefault(), "%s %s", f, l)
            f.isNotEmpty() -> f
            l.isNotEmpty() -> l
            else -> "Akilimo User"
        }
    }
}
