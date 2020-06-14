package com.iita.akilimo.entities;


import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;


@Entity
public class OperationCosts {
    @Id
    long id;

    private String costLmoAreaBasis;

    private double manualPloughCost;
    private double manualRidgeCost;
    private double manualHarrowCost;

    private double tractorPloughCost;
    private double tractorRidgeCost;
    private double tractorHarrowCost;

    private double firstWeedingOperationCost;
    private double secondWeedingOperationCost;

    
    public boolean exactManualPloughPrice;

    
    public boolean exactManualRidgePrice;

    
    public boolean exactManualHarrowPrice;

    
    public boolean exactTractorPloughPrice;

    
    public boolean exactTractorRidgePrice;

    
    public boolean exactFirstWeedingPrice;

    
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCostLmoAreaBasis() {
        return costLmoAreaBasis;
    }

    public void setCostLmoAreaBasis(String costLmoAreaBasis) {
        this.costLmoAreaBasis = costLmoAreaBasis;
    }

    public double getManualPloughCost() {
        return manualPloughCost;
    }

    public void setManualPloughCost(double manualPloughCost) {
        this.manualPloughCost = manualPloughCost;
    }

    public double getManualRidgeCost() {
        return manualRidgeCost;
    }

    public void setManualRidgeCost(double manualRidgeCost) {
        this.manualRidgeCost = manualRidgeCost;
    }

    public double getManualHarrowCost() {
        return manualHarrowCost;
    }

    public void setManualHarrowCost(double manualHarrowCost) {
        this.manualHarrowCost = manualHarrowCost;
    }

    public double getTractorPloughCost() {
        return tractorPloughCost;
    }

    public void setTractorPloughCost(double tractorPloughCost) {
        this.tractorPloughCost = tractorPloughCost;
    }

    public double getTractorRidgeCost() {
        return tractorRidgeCost;
    }

    public void setTractorRidgeCost(double tractorRidgeCost) {
        this.tractorRidgeCost = tractorRidgeCost;
    }

    public double getTractorHarrowCost() {
        return tractorHarrowCost;
    }

    public void setTractorHarrowCost(double tractorHarrowCost) {
        this.tractorHarrowCost = tractorHarrowCost;
    }

    public double getFirstWeedingOperationCost() {
        return firstWeedingOperationCost;
    }

    public void setFirstWeedingOperationCost(double firstWeedingOperationCost) {
        this.firstWeedingOperationCost = firstWeedingOperationCost;
    }

    public double getSecondWeedingOperationCost() {
        return secondWeedingOperationCost;
    }

    public void setSecondWeedingOperationCost(double secondWeedingOperationCost) {
        this.secondWeedingOperationCost = secondWeedingOperationCost;
    }

    public void setExactManualPloughPrice(boolean exactManualPloughPrice) {
        this.exactManualPloughPrice = exactManualPloughPrice;
    }

    public void setExactManualRidgePrice(boolean exactManualRidgePrice) {
        this.exactManualRidgePrice = exactManualRidgePrice;
    }

    public void setExactManualHarrowPrice(boolean exactManualHarrowPrice) {
        this.exactManualHarrowPrice = exactManualHarrowPrice;
    }

    public void setExactTractorPloughPrice(boolean exactTractorPloughPrice) {
        this.exactTractorPloughPrice = exactTractorPloughPrice;
    }

    public void setExactTractorRidgePrice(boolean exactTractorRidgePrice) {
        this.exactTractorRidgePrice = exactTractorRidgePrice;
    }

    public void setExactFirstWeedingPrice(boolean exactFirstWeedingPrice) {
        this.exactFirstWeedingPrice = exactFirstWeedingPrice;
    }

    public void setExactSecondWeedingPrice(boolean exactSecondWeedingPrice) {
        this.exactSecondWeedingPrice = exactSecondWeedingPrice;
    }
}
