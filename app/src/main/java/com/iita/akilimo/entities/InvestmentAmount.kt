package com.iita.akilimo.entities

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable


@DatabaseTable(tableName = "investment_amount")
open class InvestmentAmount {

    @DatabaseField(columnName = "id", generatedId = true)
    var id: Int? = null
    var minInvestmentAmountUSD: Double = 0.0
    var minInvestmentAmountLocal: Double = 0.0
    var investmentAmountUSD: Double = 0.0
    var investmentAmountLocal: Double = 0.0
}