package com.iita.akilimo.entities

import android.os.Parcel
import android.os.Parcelable
import com.iita.akilimo.utils.enums.EnumOperation
import kotlinx.android.parcel.Parcelize
import lombok.Data

@Parcelize
class PriceModel(
    val id: Long,
    var minPrice: Double,
    var maxPrice: Double,
    var operationName: String,
    var operation: EnumOperation
) : Parcelable {

    fun getAveragePrice(): Double {
        return (this.minPrice + this.maxPrice) / 2
    }

    fun getPriceRange(translatedSuffix: String): String {
        return "${this.minPrice} $translatedSuffix ${this.maxPrice}"
    }
}