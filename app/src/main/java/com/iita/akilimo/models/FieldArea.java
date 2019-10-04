package com.iita.akilimo.models;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
//@Getter
//@Builder
@Entity
public class FieldArea {

    @Id
    long id;

    private String fieldAreaType;
    private String singleFieldUnit;
    private String areaUnit;

    private double fieldArea;

    @Getter(AccessLevel.NONE)
    private boolean exactArea;

    public boolean isExactArea() {
        return this.exactArea;
    }
}
