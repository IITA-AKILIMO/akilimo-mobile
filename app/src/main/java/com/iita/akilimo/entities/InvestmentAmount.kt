package com.iita.akilimo.entities

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class InvestmentAmount : RealmObject() {
    @PrimaryKey
    var id: Long = 0
    var minInvestmentAmountUSD: Double = 0.0
    var minInvestmentAmountLocal: Double = 0.0
    var investmentAmountUSD: Double = 0.0
    var investmentAmountLocal: Double = 0.0
}