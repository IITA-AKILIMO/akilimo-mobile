package com.iita.akilimo.models;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@SuppressWarnings("WeakerAccess")
@Data
@Entity
public class CultivationAdvice {
    @Id
    long id;
    
    @Getter(AccessLevel.NONE)
    private boolean fertilizerAdvice;

    @Getter(AccessLevel.NONE)
    private boolean interCroppingAdvice;

    @Getter(AccessLevel.NONE)
    private boolean plantingPracticeAdvice;

    @Getter(AccessLevel.NONE)
    private boolean scheduledPlantingAdvice;


    public boolean isFertilizerAdvice() {
        return this.fertilizerAdvice;
    }

    public boolean isInterCroppingAdvice() {
        return this.interCroppingAdvice;
    }

    public boolean isPlantingPracticeAdvice() {
        return this.plantingPracticeAdvice;
    }

    public boolean isScheduledPlantingAdvice() {
        return this.scheduledPlantingAdvice;
    }
}
