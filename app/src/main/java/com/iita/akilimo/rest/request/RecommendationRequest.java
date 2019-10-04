package com.iita.akilimo.rest.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iita.akilimo.models.Fertilizer;

import java.util.List;

import lombok.Data;

@Data
public class RecommendationRequest {

    @JsonProperty("computeRequest")
    private ComputeRequest computeRequest;

    @JsonProperty("fertilizerList")
    private List<Fertilizer> fertilizerList;
}
