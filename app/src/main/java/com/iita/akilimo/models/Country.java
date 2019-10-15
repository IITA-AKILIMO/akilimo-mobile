package com.iita.akilimo.models;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import lombok.Data;

@Data
@Entity
@Deprecated
public class Country {

    @Id
    long id;
    private String countryName;
    private String countryCode;
}
