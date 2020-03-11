package com.iita.akilimo.entities;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
@Entity
public class PlantingHarvestDates {
    @Id
    long id;
    private String deviceId;

    private String plantingDate;
    private int plantingWindow;

    private String harvestDate;
    private int harvestWindow;

    @Getter(AccessLevel.NONE)
    public boolean alternativeDate;

    public boolean isAlternativeDate() {
        return this.alternativeDate;
    }
}
