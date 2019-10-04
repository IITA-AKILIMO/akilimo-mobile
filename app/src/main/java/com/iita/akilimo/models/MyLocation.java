package com.iita.akilimo.models;


import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
@Entity
public class MyLocation {

    @Id
    long id;
    public double latitude;
    public double longitude;
    public double altitude;

    public String placeName;
    public String address;
    public String countryCode;
    public String countryName;
    public String currency;

    public String areaUnit;
    public double areaSize;

    @Getter(AccessLevel.NONE)
    public boolean exactArea;

    public boolean isExactArea() {
        return this.exactArea;
    }
}