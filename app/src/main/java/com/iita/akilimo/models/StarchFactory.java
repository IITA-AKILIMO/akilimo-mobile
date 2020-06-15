package com.iita.akilimo.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.android.gms.common.util.Strings;
import com.orm.SugarRecord;


@JsonIgnoreProperties(ignoreUnknown = true)
public class StarchFactory extends SugarRecord<StarchFactory> {

    private long id;

    @JsonProperty("factoryName")
    private String factoryName;

    @JsonProperty("factoryLabel")
    private String factoryLabel;

    @JsonProperty("factoryNameCountry")
    private String factoryNameCountry;

    @JsonProperty("countryCode")
    private String countryCode;

    @JsonProperty("factoryActive")
    private boolean factoryActive;

    private boolean factorySelected;

    public boolean sellToStarchFactory() {
        return !Strings.isEmptyOrWhitespace(this.factoryName) && !this.factoryName.equalsIgnoreCase("NA");
    }
}
