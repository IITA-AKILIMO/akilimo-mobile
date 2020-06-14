package com.iita.akilimo.entities;


import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class CurrentPractice {

    @Id
    long id;

    private int weedRadioIndex;
    private String weedControlTechnique;

    private String ploughOperations;
    private String ridgeOperations;
    private String harrowOperations;
    private String weedControlOperations;

    private String ploughingMethod;
    private String ridgingMethod;
    private String harrowingMethod;

    private boolean tractorAvailable;
    private boolean tractorPlough;

    private boolean tractorHarrow;


    private boolean tractorRidger;


    private boolean usesHerbicide;


    private boolean performPloughing;


    private boolean performHarrowing;


    private boolean performRidging;

    public boolean getTractorAvailable() {
        return this.tractorAvailable;
    }

    public boolean getTractorPlough() {
        return this.tractorPlough;
    }

    public boolean getTractorHarrow() {
        return this.tractorHarrow;
    }

    public boolean getTractorRidger() {
        return this.tractorRidger;
    }

    public boolean getUsesHerbicide() {
        return this.usesHerbicide;
    }

    public boolean getPerformPloughing() {
        return this.performPloughing;
    }

    public boolean getPerformHarrowing() {
        return this.performHarrowing;
    }

    public boolean getPerformRidging() {
        return this.performRidging;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getWeedRadioIndex() {
        return weedRadioIndex;
    }

    public void setWeedRadioIndex(int weedRadioIndex) {
        this.weedRadioIndex = weedRadioIndex;
    }

    public String getWeedControlTechnique() {
        return weedControlTechnique;
    }

    public void setWeedControlTechnique(String weedControlTechnique) {
        this.weedControlTechnique = weedControlTechnique;
    }

    public String getPloughOperations() {
        return ploughOperations;
    }

    public void setPloughOperations(String ploughOperations) {
        this.ploughOperations = ploughOperations;
    }

    public String getRidgeOperations() {
        return ridgeOperations;
    }

    public void setRidgeOperations(String ridgeOperations) {
        this.ridgeOperations = ridgeOperations;
    }

    public String getHarrowOperations() {
        return harrowOperations;
    }

    public void setHarrowOperations(String harrowOperations) {
        this.harrowOperations = harrowOperations;
    }

    public String getWeedControlOperations() {
        return weedControlOperations;
    }

    public void setWeedControlOperations(String weedControlOperations) {
        this.weedControlOperations = weedControlOperations;
    }

    public String getPloughingMethod() {
        return ploughingMethod;
    }

    public void setPloughingMethod(String ploughingMethod) {
        this.ploughingMethod = ploughingMethod;
    }

    public String getRidgingMethod() {
        return ridgingMethod;
    }

    public void setRidgingMethod(String ridgingMethod) {
        this.ridgingMethod = ridgingMethod;
    }

    public String getHarrowingMethod() {
        return harrowingMethod;
    }

    public void setHarrowingMethod(String harrowingMethod) {
        this.harrowingMethod = harrowingMethod;
    }

    public boolean isTractorAvailable() {
        return tractorAvailable;
    }

    public void setTractorAvailable(boolean tractorAvailable) {
        this.tractorAvailable = tractorAvailable;
    }

    public boolean isTractorPlough() {
        return tractorPlough;
    }

    public void setTractorPlough(boolean tractorPlough) {
        this.tractorPlough = tractorPlough;
    }

    public boolean isTractorHarrow() {
        return tractorHarrow;
    }

    public void setTractorHarrow(boolean tractorHarrow) {
        this.tractorHarrow = tractorHarrow;
    }

    public boolean isTractorRidger() {
        return tractorRidger;
    }

    public void setTractorRidger(boolean tractorRidger) {
        this.tractorRidger = tractorRidger;
    }

    public boolean isUsesHerbicide() {
        return usesHerbicide;
    }

    public void setUsesHerbicide(boolean usesHerbicide) {
        this.usesHerbicide = usesHerbicide;
    }

    public boolean isPerformPloughing() {
        return performPloughing;
    }

    public void setPerformPloughing(boolean performPloughing) {
        this.performPloughing = performPloughing;
    }

    public boolean isPerformHarrowing() {
        return performHarrowing;
    }

    public void setPerformHarrowing(boolean performHarrowing) {
        this.performHarrowing = performHarrowing;
    }

    public boolean isPerformRidging() {
        return performRidging;
    }

    public void setPerformRidging(boolean performRidging) {
        this.performRidging = performRidging;
    }
}
