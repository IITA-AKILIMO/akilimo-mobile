package com.akilimo.mobile.rest.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.jetbrains.annotations.NotNull;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Recommendation implements Parcelable {
    @JsonProperty("FR")
    private String fertilizerRecText;

    @JsonProperty("IC")
    private String interCroppingRecText;

    @JsonProperty("PP")
    private String plantingPracticeRecText;

    @JsonProperty("SP")
    private String scheduledPlantingRect;

    @JsonProperty("ERR")
    private String errorMessage;

    @SuppressWarnings("unused")
    public Recommendation() {
        //required by jackson do not remove
    }

    public final static Creator<Recommendation> CREATOR = new Creator<Recommendation>() {
        public Recommendation createFromParcel(Parcel in) {
            return new Recommendation(in);
        }

        public Recommendation[] newArray(int size) {
            return (new Recommendation[size]);
        }

    };

    private Recommendation(@NotNull Parcel in) {
        this.fertilizerRecText = ((String) in.readValue((String.class.getClassLoader())));
        this.interCroppingRecText = ((String) in.readValue((String.class.getClassLoader())));
        this.plantingPracticeRecText = ((String) in.readValue((String.class.getClassLoader())));
        this.scheduledPlantingRect = ((String) in.readValue((String.class.getClassLoader())));
    }

    public void writeToParcel(@NotNull Parcel dest, int flags) {
        dest.writeValue(fertilizerRecText);
        dest.writeValue(interCroppingRecText);
        dest.writeValue(plantingPracticeRecText);
        dest.writeValue(scheduledPlantingRect);
    }

    public int describeContents() {
        return 0;
    }

    public String getFertilizerRecText() {
        return fertilizerRecText;
    }

    public void setFertilizerRecText(String fertilizerRecText) {
        this.fertilizerRecText = fertilizerRecText;
    }

    public String getInterCroppingRecText() {
        return interCroppingRecText;
    }

    public void setInterCroppingRecText(String interCroppingRecText) {
        this.interCroppingRecText = interCroppingRecText;
    }

    public String getPlantingPracticeRecText() {
        return plantingPracticeRecText;
    }

    public void setPlantingPracticeRecText(String plantingPracticeRecText) {
        this.plantingPracticeRecText = plantingPracticeRecText;
    }

    public String getScheduledPlantingRect() {
        return scheduledPlantingRect;
    }

    public void setScheduledPlantingRect(String scheduledPlantingRect) {
        this.scheduledPlantingRect = scheduledPlantingRect;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
