package com.iita.akilimo.entities;


import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
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

    @Getter(AccessLevel.NONE)
    private boolean tractorAvailable;

    @Getter(AccessLevel.NONE)
    private boolean tractorPlough;

    @Getter(AccessLevel.NONE)
    private boolean tractorHarrow;

    @Getter(AccessLevel.NONE)
    private boolean tractorRidger;

    @Getter(AccessLevel.NONE)
    private boolean usesHerbicide;

    @Getter(AccessLevel.NONE)
    private boolean performPloughing;

    @Getter(AccessLevel.NONE)
    private boolean performHarrowing;

    @Getter(AccessLevel.NONE)
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
}
