package com.iita.akilimo.entities;


import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import lombok.Data;

@Data
@Entity
public class LocationInfo {

    @Id
    long id;

    public double latitude;
    public double longitude;
    public double altitude;

    public String placeName;
    public String address;

}