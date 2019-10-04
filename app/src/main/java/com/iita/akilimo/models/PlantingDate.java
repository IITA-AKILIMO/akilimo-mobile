package com.iita.akilimo.models;


import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import lombok.Data;

@Data
@Entity
public class PlantingDate {

    @Id
    long id;
    private String plantingDate;

    private int plantingDayNumber;
    private int plantingWindow;
    private int plantingWindowIndex;
}
