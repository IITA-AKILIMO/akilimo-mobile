package com.iita.akilimo.entities

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
class InvestmentAmount {
    @Id
    var id: Long = 0
    var minInvestmentAmountUSD: Double = 0.0
    var minInvestmentAmountLocal: Double = 0.0
    var investmentAmountUSD: Double = 0.0
    var investmentAmountLocal: Double = 0.0
}