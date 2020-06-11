package com.iita.akilimo.entities;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;



@Entity
public class PlantingHarvestDates {
    @Id
    long id;
    private String deviceId;

    private String plantingDate;
    private int plantingWindow;

    private String harvestDate;
    private int harvestWindow;

    public boolean alternativeDate;

    public boolean isAlternativeDate() {
        return this.alternativeDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getPlantingDate() {
        return plantingDate;
    }

    public void setPlantingDate(String plantingDate) {
        this.plantingDate = plantingDate;
    }

    public int getPlantingWindow() {
        return plantingWindow;
    }

    public void setPlantingWindow(int plantingWindow) {
        this.plantingWindow = plantingWindow;
    }

    public String getHarvestDate() {
        return harvestDate;
    }

    public void setHarvestDate(String harvestDate) {
        this.harvestDate = harvestDate;
    }

    public int getHarvestWindow() {
        return harvestWindow;
    }

    public void setHarvestWindow(int harvestWindow) {
        this.harvestWindow = harvestWindow;
    }

    public void setAlternativeDate(boolean alternativeDate) {
        this.alternativeDate = alternativeDate;
    }
}
