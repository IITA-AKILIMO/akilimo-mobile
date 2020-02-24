package com.iita.akilimo.entities;

import android.os.Parcel;
import android.os.Parcelable;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
@Entity
@Deprecated
public class TillageOperations implements Parcelable {
    @Id
    long id;

    private String weedControlTechnique;
    private String tillageOperation;

    @Getter(AccessLevel.NONE)
    private boolean tractorAvailable;

    @Getter(AccessLevel.NONE)
    private boolean tractorPlough;

    @Getter(AccessLevel.NONE)
    private boolean tractorHarrow;

    @Getter(AccessLevel.NONE)
    private boolean tractorRidger;

    @Getter(AccessLevel.NONE)
    private boolean usesHerbicide;


    //Hold tillage operations data here
    double firstManualPlough = 0;
    double secondManualPlough = 0;
    double manualRidging = 0;
    double firstMechPlough = 0;
    double secondMechPlough = 0;
    double tractorHarrowCost = 0;
    double tractorRidging = 0;

    public TillageOperations() {

    }

    private TillageOperations(Parcel in) {
        id = in.readLong();
        weedControlTechnique = in.readString();
        tillageOperation = in.readString();
        tractorAvailable = in.readByte() != 0;
        tractorPlough = in.readByte() != 0;
        tractorHarrow = in.readByte() != 0;
        tractorRidger = in.readByte() != 0;
        usesHerbicide = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(weedControlTechnique);
        dest.writeString(tillageOperation);
        dest.writeByte((byte) (tractorAvailable ? 1 : 0));
        dest.writeByte((byte) (tractorPlough ? 1 : 0));
        dest.writeByte((byte) (tractorHarrow ? 1 : 0));
        dest.writeByte((byte) (tractorRidger ? 1 : 0));
        dest.writeByte((byte) (usesHerbicide ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TillageOperations> CREATOR = new Creator<TillageOperations>() {
        @Override
        public TillageOperations createFromParcel(Parcel in) {
            return new TillageOperations(in);
        }

        @Override
        public TillageOperations[] newArray(int size) {
            return new TillageOperations[size];
        }
    };

    public boolean getTractorAvailable() {
        return this.tractorAvailable;
    }

    public boolean getTractorPlough() {
        return this.tractorPlough;
    }

    public boolean getTractorHarrow() {
        return this.tractorHarrow;
    }

    public boolean getTractorRidger() {
        return this.tractorRidger;
    }

    public boolean getUsesHerbicide() {
        return this.usesHerbicide;
    }
}
