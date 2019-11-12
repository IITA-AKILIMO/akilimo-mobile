package com.iita.akilimo.entities;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.Data;

@Data
public class OperationCost implements Parcelable {
    private double firstManualPlough;
    private double secondManualPlough;
    private double manualRidging;

    private double firstMechPlough;
    private double secondMechPlough;
    private double tractorHarrowCost;
    private double tractorRidging;


    public OperationCost() {

    }


    public static OperationCost newInstance() {
        OperationCost oc = new OperationCost();
        oc.setFirstManualPlough(52);
        oc.setSecondManualPlough(41);
        oc.setManualRidging(21);
        oc.setFirstMechPlough(28);
        oc.setSecondMechPlough(22);
        oc.setTractorHarrowCost(22);
        oc.setTractorRidging(21);

        return oc;
    }

    private OperationCost(Parcel in) {
        firstManualPlough = in.readDouble();
        secondManualPlough = in.readDouble();
        manualRidging = in.readDouble();
        firstMechPlough = in.readDouble();
        secondMechPlough = in.readDouble();
        tractorHarrowCost = in.readDouble();
        tractorRidging = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(firstManualPlough);
        dest.writeDouble(secondManualPlough);
        dest.writeDouble(manualRidging);
        dest.writeDouble(firstMechPlough);
        dest.writeDouble(secondMechPlough);
        dest.writeDouble(tractorHarrowCost);
        dest.writeDouble(tractorRidging);
    }

    public static final Creator<OperationCost> CREATOR = new Creator<OperationCost>() {
        @Override
        public OperationCost createFromParcel(Parcel in) {
            return new OperationCost(in);
        }

        @Override
        public OperationCost[] newArray(int size) {
            return new OperationCost[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

}
