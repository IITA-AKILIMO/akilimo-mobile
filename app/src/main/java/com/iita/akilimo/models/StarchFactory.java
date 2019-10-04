package com.iita.akilimo.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.android.gms.common.util.Strings;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
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
    @Getter(AccessLevel.NONE)
    boolean factoryActive;

    @Getter(AccessLevel.NONE)
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
}
