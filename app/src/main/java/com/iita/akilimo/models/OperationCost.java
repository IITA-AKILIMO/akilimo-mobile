package com.iita.akilimo.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class OperationCost implements Parcelable {

    @JsonProperty("id")
    long id;


    @JsonProperty("listIndex")
    private long listIndex;

    @JsonProperty("operationName")
    private String operationName;

    @JsonProperty("operationType")
    private String operationType;

    @JsonProperty("minUsd")
    private double minUsd;

    @JsonProperty("maxUsd")
    private double maxUsd;

    @JsonProperty("minTzs")
    private double minTzs;

    @JsonProperty("maxTzs")
    private double maxTzs;

    @JsonProperty("minNgn")
    private double minNgn;

    @JsonProperty("maxNgn")
    private double maxNgn;

    @JsonProperty("averageNgnPrice")
    private double averageNgnPrice;

    @JsonProperty("averageTzsPrice")
    private double averageTzsPrice;

    @JsonProperty("averageUsdPrice")
    private double averageUsdPrice;

    public OperationCost() {

    }

    protected OperationCost(Parcel in) {
        id = in.readLong();
        operationName = in.readString();
        operationType = in.readString();
        minUsd = in.readDouble();
        maxUsd = in.readDouble();
        minTzs = in.readDouble();
        maxTzs = in.readDouble();
        minNgn = in.readDouble();
        maxNgn = in.readDouble();
        averageNgnPrice = in.readDouble();
        averageTzsPrice = in.readDouble();
        averageUsdPrice = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(operationName);
        dest.writeString(operationType);
        dest.writeDouble(minUsd);
        dest.writeDouble(maxUsd);
        dest.writeDouble(minTzs);
        dest.writeDouble(maxTzs);
        dest.writeDouble(minNgn);
        dest.writeDouble(maxNgn);
        dest.writeDouble(averageNgnPrice);
        dest.writeDouble(averageTzsPrice);
        dest.writeDouble(averageUsdPrice);
    }

    @Override
    public int describeContents() {
        return 0;
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
}