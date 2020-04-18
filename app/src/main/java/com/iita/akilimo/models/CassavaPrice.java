package com.iita.akilimo.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Entity
public class CassavaPrice {

    @Id
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

    @JsonProperty("createdAt")
    String createdAt;

    @JsonProperty("updatedAt")
    String updatedAt;

    @JsonProperty("averagePrice")
    double averagePrice;
}