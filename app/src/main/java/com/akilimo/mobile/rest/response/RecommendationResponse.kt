package com.akilimo.mobile.rest.response

import android.os.Parcel
import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class RecommendationResponse(
    @JsonProperty("FR") var fertilizerRecText: String? = null,
    @JsonProperty("IC") var interCroppingRecText: String? = null,
    @JsonProperty("PP") var plantingPracticeRecText: String? = null,
    @JsonProperty("SP") var scheduledPlantingRecText: String? = null,
    @JsonProperty("ERR") var errorMessage: String? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        fertilizerRecText = parcel.readString(),
        interCroppingRecText = parcel.readString(),
        plantingPracticeRecText = parcel.readString(),
        scheduledPlantingRecText = parcel.readString(),
        errorMessage = parcel.readString()
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(fertilizerRecText)
        dest.writeString(interCroppingRecText)
        dest.writeString(plantingPracticeRecText)
        dest.writeString(scheduledPlantingRecText)
        dest.writeString(errorMessage)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<RecommendationResponse> {
        override fun createFromParcel(parcel: Parcel): RecommendationResponse {
            return RecommendationResponse(parcel)
        }

        override fun newArray(size: Int): Array<RecommendationResponse?> {
            return arrayOfNulls(size)
        }
    }
}
