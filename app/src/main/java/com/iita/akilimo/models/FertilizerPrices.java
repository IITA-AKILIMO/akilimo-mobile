package com.iita.akilimo.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class FertilizerPrices {

    @Id
    @JsonProperty("id")
    long id;

    @JsonProperty("recordId")
    int recordId;
    @JsonProperty("priceId")
    int priceId;

    @JsonProperty("minUsd")
    private double minUsd;

    @JsonProperty("maxUsd")
    private double maxUsd;

    @JsonProperty("pricePerBag")
    private double pricePerBag;

    @JsonProperty("active")
    private boolean active;

    @JsonProperty("priceRange")
    private String priceRange;

    @JsonProperty("country")
    private String country;

    @Unique
    @JsonProperty("fertilizerCountry")
    private String fertilizerCountry;

    @JsonProperty("description")
    private String description;

    public boolean isActive() {
        return this.active;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public int getPriceId() {
        return priceId;
    }

    public void setPriceId(int priceId) {
        this.priceId = priceId;
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

    public double getPricePerBag() {
        return pricePerBag;
    }

    public void setPricePerBag(double pricePerBag) {
        this.pricePerBag = pricePerBag;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getPriceRange() {
        return priceRange;
    }

    public void setPriceRange(String priceRange) {
        this.priceRange = priceRange;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getFertilizerCountry() {
        return fertilizerCountry;
    }

    public void setFertilizerCountry(String fertilizerCountry) {
        this.fertilizerCountry = fertilizerCountry;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}