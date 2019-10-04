package com.iita.akilimo.models;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import lombok.Data;


@Data
@Entity
public class HarvestDate {


    @Id
    long id;

    private String harvestDate;

    private int harvestDayNumber;
    private int harvestWindow;
    private int harvestWindowIndex;

}
