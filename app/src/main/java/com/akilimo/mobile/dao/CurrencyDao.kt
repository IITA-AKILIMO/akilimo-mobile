package com.akilimo.mobile.dao

import androidx.room.*
import com.akilimo.mobile.entities.AkilimoCurrency

@Dao
interface CurrencyDao {

    @Query("SELECT * FROM currencies")
    fun listAll(): List<AkilimoCurrency>

    @Query("SELECT * FROM currencies LIMIT 1")
    fun findOne(): AkilimoCurrency?

    @Query("SELECT * FROM currencies where currency_code=:currencyCode LIMIT 1")
    fun findOneByCurrencyCode(currencyCode: String): AkilimoCurrency

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(currencyList: List<AkilimoCurrency>):LongArray?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg currencies: AkilimoCurrency)

    @Update
    fun update(vararg currency: AkilimoCurrency)

    @Delete
    fun delete(currency: AkilimoCurrency?)
    @Query("DELETE FROM currencies")
    fun deleteAll()
}
