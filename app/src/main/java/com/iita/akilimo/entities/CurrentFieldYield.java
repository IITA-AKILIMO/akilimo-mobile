package com.iita.akilimo.entities;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Transient;
import lombok.Data;

@Data
@Entity
public class CurrentFieldYield {

    @Id
    long id;
    private double yieldAmount;
    private String fieldYieldLabel;

    @Transient
    private int imageId;
}
