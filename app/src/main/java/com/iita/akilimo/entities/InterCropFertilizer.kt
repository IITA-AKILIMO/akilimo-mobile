package com.iita.akilimo.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@Deprecated("Too much duplication move to common fertilizer")
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity(
    tableName = "intercrop_fertilizer",
    indices = [Index(value = ["fertilizerCountry"], unique = true)]
)
open class InterCropFertilizer() : Parcelable {

    @JsonProperty("id")
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

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

    constructor(parcel: Parcel) : this() {
        imageId = parcel.readInt()
        fertilizerId = parcel.readInt()
        name = parcel.readString()
        fertilizerType = parcel.readString()
        weight = parcel.readInt()
        price = parcel.readDouble()
        fertilizerCountry = parcel.readString()
        currency = parcel.readString()
        useCase = parcel.readString()
        countryCode = parcel.readString()
        priceRange = parcel.readString()
        pricePerBag = parcel.readDouble()
        kContent = parcel.readInt()
        nContent = parcel.readInt()
        pContent = parcel.readInt()
        available = parcel.readByte() != 0.toByte()
        cimAvailable = parcel.readByte() != 0.toByte()
        cisAvailable = parcel.readByte() != 0.toByte()
        selected = parcel.readByte() != 0.toByte()
        exactPrice = parcel.readByte() != 0.toByte()
        custom = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(imageId)
        parcel.writeInt(fertilizerId)
        parcel.writeString(name)
        parcel.writeString(fertilizerType)
        parcel.writeInt(weight)
        parcel.writeValue(price)
        parcel.writeString(fertilizerCountry)
        parcel.writeString(currency)
        parcel.writeString(useCase)
        parcel.writeString(countryCode)
        parcel.writeString(priceRange)
        parcel.writeDouble(pricePerBag)
        parcel.writeInt(kContent)
        parcel.writeInt(nContent)
        parcel.writeInt(pContent)
        parcel.writeByte(if (available) 1 else 0)
        parcel.writeByte(if (cimAvailable) 1 else 0)
        parcel.writeByte(if (cisAvailable) 1 else 0)
        parcel.writeByte(if (selected) 1 else 0)
        parcel.writeByte(if (exactPrice) 1 else 0)
        parcel.writeByte(if (custom) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<InterCropFertilizer> {
        override fun createFromParcel(parcel: Parcel): InterCropFertilizer {
            return InterCropFertilizer(parcel)
        }

        override fun newArray(size: Int): Array<InterCropFertilizer?> {
            return arrayOfNulls(size)
        }
    }
}