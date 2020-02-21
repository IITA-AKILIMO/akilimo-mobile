package com.iita.akilimo.entities;


import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
@Entity
public class OperationCosts {
    @Id
    long id;

    private double manualPloughCost;
    private double manualRidgeCost;
    private double manualHarrowCost;

    private double tractorPloughCost;
    private double tractorRidgeCost;
    private double tractorHarrowCost;

    private double firstWeedingOperationCost;
    private double secondWeedingOperationCost;

    @Getter(AccessLevel.NONE)
    public boolean exactManualPloughPrice;

    @Getter(AccessLevel.NONE)
    public boolean exactManualRidgePrice;

    @Getter(AccessLevel.NONE)
    public boolean exactManualHarrowPrice;

    @Getter(AccessLevel.NONE)
    public boolean exactTractorPloughPrice;

    @Getter(AccessLevel.NONE)
    public boolean exactTractorRidgePrice;

    @Getter(AccessLevel.NONE)
    public boolean exactFirstWeedingPrice;

    @Getter(AccessLevel.NONE)
    public boolean exactSecondWeedingPrice;

    public boolean isExactManualPloughPrice() {
        return this.exactManualPloughPrice;
    }
    public boolean isExactManualRidgePrice() {
        return this.exactManualRidgePrice;
    }
    public boolean isExactManualHarrowPrice() {
        return this.exactManualHarrowPrice;
    }

    public boolean isExactTractorPloughPrice() {
        return this.exactTractorPloughPrice;
    }
    public boolean isExactTractorRidgePrice() {
        return this.exactTractorRidgePrice;
    }

    public boolean isExactFirstWeedingPrice() {
        return this.exactFirstWeedingPrice;
    }
    public boolean isExactSecondWeedingPrice() {
        return this.exactSecondWeedingPrice;
    }

    public OperationCosts() {
        /* required empty constructor*/
    }
}
