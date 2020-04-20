package com.iita.akilimo.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Entity
@Data
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

    @Getter(AccessLevel.NONE)
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

}