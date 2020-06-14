package com.iita.akilimo.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getListIndex() {
        return listIndex;
    }

    public void setListIndex(long listIndex) {
        this.listIndex = listIndex;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public double getMinUsd() {
        return minUsd;
    }

    public void setMinUsd(double minUsd) {
        this.minUsd = minUsd;
    }

    public double getMaxUsd() {
        return maxUsd;
    }

    public void setMaxUsd(double maxUsd) {
        this.maxUsd = maxUsd;
    }

    public double getMinTzs() {
        return minTzs;
    }

    public void setMinTzs(double minTzs) {
        this.minTzs = minTzs;
    }

    public double getMaxTzs() {
        return maxTzs;
    }

    public void setMaxTzs(double maxTzs) {
        this.maxTzs = maxTzs;
    }

    public double getMinNgn() {
        return minNgn;
    }

    public void setMinNgn(double minNgn) {
        this.minNgn = minNgn;
    }

    public double getMaxNgn() {
        return maxNgn;
    }

    public void setMaxNgn(double maxNgn) {
        this.maxNgn = maxNgn;
    }

    public double getAverageNgnPrice() {
        return averageNgnPrice;
    }

    public void setAverageNgnPrice(double averageNgnPrice) {
        this.averageNgnPrice = averageNgnPrice;
    }

    public double getAverageTzsPrice() {
        return averageTzsPrice;
    }

    public void setAverageTzsPrice(double averageTzsPrice) {
        this.averageTzsPrice = averageTzsPrice;
    }

    public double getAverageUsdPrice() {
        return averageUsdPrice;
    }

    public void setAverageUsdPrice(double averageUsdPrice) {
        this.averageUsdPrice = averageUsdPrice;
    }
}