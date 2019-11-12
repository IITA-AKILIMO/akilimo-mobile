package com.iita.akilimo.models;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
@Entity
public class CroppingSystem {

    @Id
    long id;
    private String croppingSystem;

    @Getter(AccessLevel.NONE)
    private boolean skipRecommendation;


    public boolean getSkipRecommendation() {
        return this.skipRecommendation;
    }


    public boolean provideInterCroppingRecommendation() {
        return !this.croppingSystem.equals("NA");
    }
}
