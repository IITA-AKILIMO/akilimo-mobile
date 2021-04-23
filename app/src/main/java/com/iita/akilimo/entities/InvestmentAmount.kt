package com.iita.akilimo.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "investment_amount")
open class InvestmentAmount {

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
    var minInvestmentAmountUSD: Double = 0.0
    var minInvestmentAmountLocal: Double = 0.0
    var investmentAmountUSD: Double = 0.0
    var investmentAmountLocal: Double = 0.0
    var fieldSize: Double = 0.0
}
