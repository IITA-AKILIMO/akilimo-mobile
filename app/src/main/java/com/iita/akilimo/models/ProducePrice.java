package com.iita.akilimo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.orm.SugarRecord;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProducePrice extends SugarRecord<ProducePrice> {

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
}