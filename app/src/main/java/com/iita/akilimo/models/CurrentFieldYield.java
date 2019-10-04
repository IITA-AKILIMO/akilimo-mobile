package com.iita.akilimo.models;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Transient;
import lombok.Data;

@Data
@Entity
public class CurrentFieldYield {

    @Id
    long id;
    private String yieldDesc;
    private double yieldAmount;

    @Transient
    private int imageId;
    @Transient
    private String fieldYieldLabel;
}
