package com.iita.akilimo.models

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
@JsonIgnoreProperties(ignoreUnknown = true)
open class Fertilizer(
    @Id
    @JsonIgnore
    var id: Long = 0,

    var imageId: Int = 0,

    @JsonProperty("fertilizerId")
    var fertilizerId: Long = 0,

    @JsonProperty("name")
    var name: String? = null,


    @JsonProperty("type")
    var fertilizerType: String? = null,

    @JsonProperty("weight")
    var weight: Double = 0.0,

    @JsonProperty("price")
    var price: Double? = null,

    @Unique
    @JsonProperty("fertilizerCountry")
    var fertilizerCountry: String? = null,

    @JsonProperty("currency")
    var currency: String? = null,

    @JsonProperty("useCase")
    var useCase: String? = null,
    var countryCode: String? = null,
    var priceRange: String? = null,
    var pricePerBag: Double = 0.0,

    @JsonProperty("kcontent")
    var kContent: Int = 0,

    @JsonProperty("ncontent")
    var nContent: Int = 0,

    @JsonProperty("pcontent")
    var pContent: Int = 0,

    @JsonProperty("available")
    var available: Boolean = false,

    @JsonProperty("cimAvailable")
    var cimAvailable: Boolean = false,

    @JsonProperty("cisAvailable")
    var cisAvailable: Boolean = false,
    var selected: Boolean = false,
    var exactPrice: Boolean = false,

    @JsonProperty("custom")
    var custom: Boolean = false
) : Parcelable