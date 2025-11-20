package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.akilimo.mobile.dto.OperationEntry
import com.akilimo.mobile.enums.EnumAdvice
import com.akilimo.mobile.enums.EnumAreaUnit
import com.akilimo.mobile.enums.EnumInvestmentPref
import java.time.LocalDate
import java.util.Locale

@Entity(
    tableName = "akilimo_users",
    indices = [Index(value = ["user_name"], unique = true)]
)
open class AkilimoUser {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int? = null

    @ColumnInfo(name = "device_token")
    var deviceToken: String? = null

    @ColumnInfo(name = "user_name")
    var userName: String? = null

    @ColumnInfo(name = "first_name")
    var firstName: String? = null

    @ColumnInfo(name = "last_name")
    var lastName: String? = null

    @ColumnInfo(name = "email")
    var email: String? = null

    @ColumnInfo(name = "mobile_number")
    var mobileNumber: String? = null

    @ColumnInfo(name = "mobile_country_code")
    var mobileCountryCode: String? = null

    @ColumnInfo(name = "farm_name")
    var farmName: String? = null

    @ColumnInfo(name = "farm_country")
    var farmCountry: String? = null

    @ColumnInfo(name = "farm_size_unit")
    var enumAreaUnit: EnumAreaUnit? = null

    @ColumnInfo(name = "farm_size")
    var farmSize: Double? = null

    @ColumnInfo(name = "custom_farm_size")
    var customFarmSize: Boolean? = null

    @ColumnInfo(name = "farm_description")
    var farmDescription: String? = null

    @ColumnInfo(name = "latitude")
    var latitude: Double = 0.0

    @ColumnInfo(name = "longitude")
    var longitude: Double = 0.0

    @ColumnInfo(name = "altitude")
    var altitude: Double = 0.0

    @ColumnInfo(name = "zoom_level")
    var zoomLevel: Double = 0.0

    @ColumnInfo(name = "gender")
    var gender: String? = null

    @ColumnInfo(name = "akilimo_interest")
    var akilimoInterest: String? = null

    @ColumnInfo(name = "send_email")
    var sendEmail: Boolean = false

    @ColumnInfo(name = "send_sms")
    var sendSms: Boolean = false

    @ColumnInfo(name = "language_code")
    var languageCode: String? = null

    @ColumnInfo(name = "risk_att")
    var riskAtt: Int = 0

    @ColumnInfo(name = "planting_date")
    var plantingDate: LocalDate? = null

    @ColumnInfo(name = "harvest_date")
    var harvestDate: LocalDate? = null

    @ColumnInfo(name = "planting_flex")
    var plantingFlex: Long = 0L

    @ColumnInfo(name = "harvest_flex")
    var harvestFlex: Long = 0L

    @ColumnInfo(name = "provided_alternative_date")
    var providedAlterNativeDate: Boolean = false

    @ColumnInfo(name = "tillage_operations")
    var tillageOperations: List<OperationEntry> = emptyList()

    @ColumnInfo(name = "investment_preferences")
    var investmentPref: EnumInvestmentPref? = null

    @ColumnInfo(name = "active_use_case")
    var activeAdvise: EnumAdvice? = null

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