package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Query
import com.akilimo.mobile.entities.AkilimoCurrency

@Dao
interface CurrencyDao : BaseDao<AkilimoCurrency> {

    @Query("SELECT * FROM currencies")
    fun listAll(): List<AkilimoCurrency>

    @Query("SELECT * FROM currencies LIMIT 1")
    fun findOne(): AkilimoCurrency?

    @Query("SELECT * FROM currencies where currency_code=:currencyCode LIMIT 1")
    fun findOneByCurrencyCode(currencyCode: String): AkilimoCurrency?
}
