package com.iita.akilimo.models

import android.os.Parcel
import android.os.Parcelable
import io.objectbox.annotation.Entity

@Entity
class InterCropFertilizer() : Fertilizer() {
    constructor(parcel: Parcel) : this() {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
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