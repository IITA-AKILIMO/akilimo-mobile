package com.iita.akilimo.models;


import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import lombok.Data;

@Deprecated
@Data
//@Entity
public class UnitOfSale {

    @Id
    long id;
    private String unitOfSale;
    private String unitOfSaleText;
    private int unitWeightValue;
}