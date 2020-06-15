package com.iita.akilimo.entities

import com.orm.SugarRecord


class InvestmentAmount : SugarRecord<InvestmentAmount>() {

    var id: Long = 0
    var minInvestmentAmountUSD: Double = 0.0
    var minInvestmentAmountLocal: Double = 0.0
    var investmentAmountUSD: Double = 0.0
    var investmentAmountLocal: Double = 0.0
}