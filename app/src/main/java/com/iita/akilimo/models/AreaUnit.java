package com.iita.akilimo.models;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import lombok.Data;

@Data
@Entity
@Deprecated
public class AreaUnit {

    @Id
    long id;

    @Deprecated
    private String areaUnit;
}
