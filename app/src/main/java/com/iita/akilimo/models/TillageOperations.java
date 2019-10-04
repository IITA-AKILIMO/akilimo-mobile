package com.iita.akilimo.models;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@SuppressWarnings("WeakerAccess")
@Data
@Entity
public class TillageOperations {

    @Id
    long id;
    private String operationName;

    @Getter(AccessLevel.NONE)
    private boolean tractorAvailable;

    @Getter(AccessLevel.NONE)
    private boolean tractorPlough;

    @Getter(AccessLevel.NONE)
    private boolean tractorHarrow;

    @Getter(AccessLevel.NONE)
    private boolean tractorRidger;

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

}
