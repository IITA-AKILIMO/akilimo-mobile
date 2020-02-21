package com.iita.akilimo.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Fertilizer implements Parcelable {


    @Id
    @JsonIgnore
    long id;

    private int imageId;

    @JsonProperty("fertilizerId")
    private int fertilizerId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    public String type;

    @JsonProperty("weight")
    private int weight;

    @JsonProperty("price")
    private String price;

    @Unique
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
    @Getter(AccessLevel.NONE)
    @JsonProperty("available")
    private boolean available;

    @JsonProperty("cimAvailable")
    private boolean cimAvailable;
    @JsonProperty("cisAvailable")
    private boolean cisAvailable;

    @Getter(AccessLevel.NONE)
    private boolean selected;

    @Getter(AccessLevel.NONE)
    private boolean exactPrice;

    @Getter(AccessLevel.NONE)
    @JsonProperty("custom")
    private boolean custom;

    public boolean isAvailable() {
        return this.available;
    }

    public boolean isCimAvailable() {
        return this.cimAvailable;
    }

    public boolean isCisAvailable() {
        return this.cisAvailable;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public boolean isExactPrice() {
        return this.exactPrice;
    }

    public boolean isCustom() {
        return this.custom;
    }


    public Fertilizer() {
        /* Required for mapping an the likes */
    }

    protected Fertilizer(Parcel in) {
        id = in.readLong();
        fertilizerId = in.readInt();
        name = in.readString();
        type = in.readString();
        weight = in.readInt();
        price = in.readString();
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
        dest.writeString(type);
        dest.writeInt(weight);
        dest.writeString(price);
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
