package com.iita.akilimo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Date;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class ProducePrice {

    @Id
    @JsonIgnore
    long id;

    @JsonProperty("priceIndex")
    long priceIndex;

    @JsonProperty("priceId")
    long priceId;

    @JsonProperty("country")
    String country;

    @JsonProperty("minLocalPrice")
    double minLocalPrice;

    @JsonProperty("maxLocalPrice")
    double maxLocalPrice;

    @JsonProperty("minUsd")
    double minUsd;

    @JsonProperty("maxUsd")
    double maxUsd;

    @JsonProperty("active")
    boolean active;

    @JsonProperty("averagePrice")
    double averagePrice;

    @JsonProperty("createdAt")
    Date createdAt;

    @JsonProperty("updatedAt")
    Date updatedAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPriceIndex() {
        return priceIndex;
    }

    public void setPriceIndex(long priceIndex) {
        this.priceIndex = priceIndex;
    }

    public long getPriceId() {
        return priceId;
    }

    public void setPriceId(long priceId) {
        this.priceId = priceId;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getMinLocalPrice() {
        return minLocalPrice;
    }

    public void setMinLocalPrice(double minLocalPrice) {
        this.minLocalPrice = minLocalPrice;
    }

    public double getMaxLocalPrice() {
        return maxLocalPrice;
    }

    public void setMaxLocalPrice(double maxLocalPrice) {
        this.maxLocalPrice = maxLocalPrice;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public double getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(double averagePrice) {
        this.averagePrice = averagePrice;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}