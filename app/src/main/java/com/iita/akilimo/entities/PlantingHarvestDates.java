package com.iita.akilimo.entities;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import lombok.Data;

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
}
