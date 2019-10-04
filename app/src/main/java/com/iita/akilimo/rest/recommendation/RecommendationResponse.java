package com.iita.akilimo.rest.recommendation;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecommendationResponse implements Parcelable {
    @JsonProperty("FR")
    private String fertilizerRecText;

    @JsonProperty("IC")
    private String interCroppingRecText;

    @JsonProperty("PP")
    private String plantingPracticeRecText;

    @JsonProperty("SP")
    private String scheduledPlantingRect;

    @SuppressWarnings("unused")
    public RecommendationResponse() {
        //required by jackson do not remove
    }
    public final static Creator<RecommendationResponse> CREATOR = new Creator<RecommendationResponse>() {
        public RecommendationResponse createFromParcel(Parcel in) {
            return new RecommendationResponse(in);
        }

        public RecommendationResponse[] newArray(int size) {
            return (new RecommendationResponse[size]);
        }

    };

    private RecommendationResponse(Parcel in) {
        this.fertilizerRecText = ((String) in.readValue((String.class.getClassLoader())));
        this.interCroppingRecText = ((String) in.readValue((String.class.getClassLoader())));
        this.plantingPracticeRecText = ((String) in.readValue((String.class.getClassLoader())));
        this.scheduledPlantingRect = ((String) in.readValue((String.class.getClassLoader())));
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(fertilizerRecText);
        dest.writeValue(interCroppingRecText);
        dest.writeValue(plantingPracticeRecText);
        dest.writeValue(scheduledPlantingRect);
    }

    public int describeContents() {
        return 0;
    }
}
