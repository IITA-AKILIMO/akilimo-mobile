package com.iita.akilimo.entities;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import lombok.Data;

@Data
@Entity
public class MarketOutlet {
    @Id
    long id;

    private String produceType;
    private String starchFactory;

    private double unitPriceUSD;
    private double unitPriceLocalCurrency;

    private String unitOfSale;
    private String unitOfSaleText;
    private int unitWeightValue;
}
