package com.iita.akilimo.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.orm.SugarRecord;


@JsonIgnoreProperties(ignoreUnknown = true)
public class FertilizerPrices extends SugarRecord<FertilizerPrices> {

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

    @JsonProperty("fertilizerCountry")
    private String fertilizerCountry;

    @JsonProperty("description")
    private String description;

}