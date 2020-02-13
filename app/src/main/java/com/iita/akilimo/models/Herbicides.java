package com.iita.akilimo.models;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Deprecated
@Data
//@Entity
public class Herbicides {

    @Id
    long id;


    @Getter(AccessLevel.NONE)
    private boolean herbicideUsed;

    public boolean getHerbicideUsed() {
        return this.herbicideUsed;
    }
}
