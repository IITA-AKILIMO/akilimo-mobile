package com.iita.akilimo.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.android.gms.common.util.Strings;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class StarchFactory {

    @Id
    long id;

    @JsonProperty("factoryName")
    String factoryName;

    @JsonProperty("factoryLabel")
    String factoryLabel;

    @Unique
    @JsonProperty("factoryNameCountry")
    String factoryNameCountry;

    @JsonProperty("countryCode")
    String countryCode;

    @JsonProperty("factoryActive")
    boolean factoryActive;

    boolean factorySelected;

    public boolean isFactoryActive() {
        return this.factoryActive;
    }

    public boolean isFactorySelected() {
        return this.factorySelected;
    }

    public boolean sellToStarchFactory() {
        return !Strings.isEmptyOrWhitespace(this.factoryName) && !this.factoryName.equalsIgnoreCase("NA");
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFactoryName() {
        return factoryName;
    }

    public void setFactoryName(String factoryName) {
        this.factoryName = factoryName;
    }

    public String getFactoryLabel() {
        return factoryLabel;
    }

    public void setFactoryLabel(String factoryLabel) {
        this.factoryLabel = factoryLabel;
    }

    public String getFactoryNameCountry() {
        return factoryNameCountry;
    }

    public void setFactoryNameCountry(String factoryNameCountry) {
        this.factoryNameCountry = factoryNameCountry;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public void setFactoryActive(boolean factoryActive) {
        this.factoryActive = factoryActive;
    }

    public void setFactorySelected(boolean factorySelected) {
        this.factorySelected = factorySelected;
    }
}
