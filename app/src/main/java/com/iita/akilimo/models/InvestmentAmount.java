package com.iita.akilimo.models;


import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import lombok.Data;

@Data
@Entity
public class InvestmentAmount {

    @Id
    long id;

    private double minInvestmentAmountUSD;
    private double minInvestmentAmountLocal;

    private double investmentAmountUSD;
    private double investmentAmountLocal;
}