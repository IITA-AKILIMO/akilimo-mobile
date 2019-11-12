package com.iita.akilimo.models;


import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import lombok.Data;

@Data
@Entity
public class MaizeUnitOfSale {

    @Id
    long id;

    private String maizeProductType;
    private String unitOfSale;
    private String unitSellingPrice;
}