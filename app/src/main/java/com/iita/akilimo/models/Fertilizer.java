package com.iita.akilimo.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.orm.SugarRecord;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Fertilizer extends SugarRecord<Fertilizer> implements Parcelable {


    @JsonIgnore
    long id;

    private int imageId;

    @JsonProperty("fertilizerId")
    private int fertilizerId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    public String fertilizerType;

    @JsonProperty("weight")
    private int weight;

    @JsonProperty("price")
    private Double price;

    @JsonProperty("fertilizerCountry")
    private String fertilizerCountry;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("useCase")
    private String useCase;

    private String countryCode;
    private String priceRange;
    private double pricePerBag;

    @JsonProperty("kcontent")
    private int kContent;

    @JsonProperty("ncontent")
    private int nContent;

    @JsonProperty("pcontent")
    private int pContent;

    /***---Boolean fields here---**/
    @JsonProperty("available")
    private boolean available;

    @JsonProperty("cimAvailable")
    private boolean cimAvailable;
    @JsonProperty("cisAvailable")
    private boolean cisAvailable;

    private boolean selected;
    private boolean exactPrice;

    @JsonProperty("custom")
    private boolean custom;

    public Fertilizer() {
        /* Required for mapping an the likes */
    }

    protected Fertilizer(Parcel in) {
        id = in.readLong();
        fertilizerId = in.readInt();
        name = in.readString();
        fertilizerType = in.readString();
        weight = in.readInt();
        price = in.readDouble();
        currency = in.readString();
        countryCode = in.readString();
        priceRange = in.readString();
        pricePerBag = in.readDouble();
        kContent = in.readInt();
        nContent = in.readInt();
        pContent = in.readInt();
        available = in.readByte() != 0;
        selected = in.readByte() != 0;
        custom = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeInt(fertilizerId);
        dest.writeString(name);
        dest.writeString(fertilizerType);
        dest.writeInt(weight);
        dest.writeDouble(price);
        dest.writeString(currency);
        dest.writeString(countryCode);
        dest.writeString(priceRange);
        dest.writeDouble(pricePerBag);
        dest.writeInt(kContent);
        dest.writeInt(nContent);
        dest.writeInt(pContent);
        dest.writeByte((byte) (available ? 1 : 0));
        dest.writeByte((byte) (selected ? 1 : 0));
        dest.writeByte((byte) (custom ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Fertilizer> CREATOR = new Creator<Fertilizer>() {
        @Override
        public Fertilizer createFromParcel(Parcel in) {
            return new Fertilizer(in);
        }

        @Override
        public Fertilizer[] newArray(int size) {
            return new Fertilizer[size];
        }
    };
}