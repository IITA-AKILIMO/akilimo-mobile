package com.iita.akilimo.models;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import lombok.Data;

@Data
@Entity
public class OperationCosts {

    @Id
    long id;
    private String opFirstManualPlough;
    private String opFirstTractorPlough;
    private String opSecondManualPlough;
    private String opSecondTractorPlough;
    private String opTractorHarrow;
    private String opManualRidging;
    private String opTractorRidging;
    private String opManualWeeding;
    private String opHerbicideWeeding;
}
