package com.iita.akilimo.models;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import lombok.Data;

@Data
@Entity
public class MaizePerformance {

    @Id
    long id;

    private String maizePerformance;
    private String performanceValue;
}
