package com.iita.akilimo.entities;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
@Entity
public class TillageOperations {
    @Id
    long id;

    private String weedControlTechnique;
    private String tillageOperation;

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
}
