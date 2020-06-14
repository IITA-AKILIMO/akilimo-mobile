package com.iita.akilimo.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;
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
    public String fertilizerType;

    @JsonProperty("weight")
    private int weight;

    @JsonProperty("price")
    private Double price;

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


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public int getFertilizerId() {
        return fertilizerId;
    }

    public void setFertilizerId(int fertilizerId) {
        this.fertilizerId = fertilizerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFertilizerType() {
        return fertilizerType;
    }

    public void setFertilizerType(String fertilizerType) {
        this.fertilizerType = fertilizerType;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getFertilizerCountry() {
        return fertilizerCountry;
    }

    public void setFertilizerCountry(String fertilizerCountry) {
        this.fertilizerCountry = fertilizerCountry;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getUseCase() {
        return useCase;
    }

    public void setUseCase(String useCase) {
        this.useCase = useCase;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getPriceRange() {
        return priceRange;
    }

    public void setPriceRange(String priceRange) {
        this.priceRange = priceRange;
    }

    public double getPricePerBag() {
        return pricePerBag;
    }

    public void setPricePerBag(double pricePerBag) {
        this.pricePerBag = pricePerBag;
    }

    public int getKContent() {
        return kContent;
    }

    public void setKContent(int kContent) {
        this.kContent = kContent;
    }

    public int getNContent() {
        return nContent;
    }

    public void setNContent(int nContent) {
        this.nContent = nContent;
    }

    public int getPContent() {
        return pContent;
    }

    public void setPContent(int pContent) {
        this.pContent = pContent;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void setCimAvailable(boolean cimAvailable) {
        this.cimAvailable = cimAvailable;
    }

    public void setCisAvailable(boolean cisAvailable) {
        this.cisAvailable = cisAvailable;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setExactPrice(boolean exactPrice) {
        this.exactPrice = exactPrice;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isCimAvailable() {
        return cimAvailable;
    }

    public boolean isCisAvailable() {
        return cisAvailable;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean isExactPrice() {
        return exactPrice;
    }

    public boolean isCustom() {
        return custom;
    }
}