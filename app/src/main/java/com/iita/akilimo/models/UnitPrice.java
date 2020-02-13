package com.iita.akilimo.models;


import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import lombok.Data;

@Deprecated
@Data
public class UnitPrice {

    @Id
    long id;
    private double unitPriceUSD;

    private double unitPriceLocalCurrency;
}