package com.iita.akilimo.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DateRange {

    @JsonProperty("displayValue")
    public String displayValue;
    @JsonProperty("windowValue")
    public int windowValue;
}
