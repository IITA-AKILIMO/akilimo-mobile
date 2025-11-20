package com.akilimo.mobile.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize

@Entity(tableName = "intercrop_fertilizer", indices = [Index(value = ["fertilizerCountry"], unique = true)])
open class InterCropFertilizer : Parcelable {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

    var imageId = 0

    
    var fertilizerKey: String? = null

    
    var fertilizerId = 0

    
    var name: String? = null


    
    var fertilizerType: String? = null

    
    var weight = 0

    
    var price: Double = 0.0


    
    var fertilizerCountry: String? = null

    
    var currency: String? = null

    
    var useCase: String? = null

    var countryCode: String? = null

    var priceRange: String? = null

    var pricePerBag = 0.0

    
    var kContent = 0

    
    var nContent = 0

    
    var pContent = 0

    /***---Boolean fields here--- */
    
    var available = false

    
    var cimAvailable = false

    
    var cisAvailable = false

    var selected = false

    var exactPrice = false

    
    var custom = false
}
