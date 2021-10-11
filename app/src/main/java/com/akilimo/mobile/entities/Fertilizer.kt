package com.akilimo.mobile.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity(tableName = "fertilizer", indices = [Index(value = ["fertilizerCountry"], unique = true)])
open class Fertilizer : Parcelable {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

    var imageId = 0

    @JsonProperty("fertilizerId")
    var fertilizerId = 0

    @JsonProperty("name")
    var name: String? = null


    @JsonProperty("type")
    var fertilizerType: String? = null

    @JsonProperty("weight")
    var weight = 0

    @JsonProperty("price")
    var price: Double = 0.0


    @JsonProperty("fertilizerCountry")
    var fertilizerCountry: String? = null

    @JsonProperty("currency")
    var currency: String? = null

    @JsonProperty("useCase")
    var useCase: String? = null

    var countryCode: String? = null

    var priceRange: String? = null

    var pricePerBag = 0.0

    @JsonProperty("kcontent")
    var kContent = 0

    @JsonProperty("ncontent")
    var nContent = 0

    @JsonProperty("pcontent")
    var pContent = 0

    /***---Boolean fields here--- */
    @JsonProperty("available")
    var available = false

    @JsonProperty("cimAvailable")
    var cimAvailable = false

    @JsonProperty("cisAvailable")
    var cisAvailable = false

    var selected = false

    var exactPrice = false

    @JsonProperty("custom")
    var custom = false
}
