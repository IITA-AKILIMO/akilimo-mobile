package com.iita.akilimo.entities;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import lombok.Data;

@Data
@Entity
public class MaizePerformance {

    @Id
    long id;

    private int performanceRadioIndex;
    private String maizePerformance;
    private String performanceValue;
}
