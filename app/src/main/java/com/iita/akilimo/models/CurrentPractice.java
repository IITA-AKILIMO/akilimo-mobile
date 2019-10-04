package com.iita.akilimo.models;


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

    private String ploughOperations;
    private String ridgeOperations;
    private String harrowOperations;
    private String weedControlOperations;

    private String ploughingMethod;
    private String ridgingMethod;
    private String harrowingMethod;

    @Getter(AccessLevel.NONE)
    private boolean performPloughing;

    @Getter(AccessLevel.NONE)
    private boolean performHarrowing;

    @Getter(AccessLevel.NONE)
    private boolean performRidging;

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
