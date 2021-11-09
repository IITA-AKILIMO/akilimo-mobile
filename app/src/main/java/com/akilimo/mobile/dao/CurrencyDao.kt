package com.akilimo.mobile.dao

import androidx.room.*
import com.akilimo.mobile.entities.Currency

@Dao
interface CurrencyDao {

    @Query("SELECT * FROM currency")
    fun listAll(): List<Currency>

    @Query("SELECT * FROM currency LIMIT 1")
    fun findOne(): Currency?

    @Query("SELECT * FROM currency where currencyCode=:currencyCode LIMIT 1")
    fun findOneByCurrencyCode(currencyCode: String): Currency

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(currencyList: List<Currency>):LongArray?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg currencies: Currency)

    @Update
    fun update(vararg currency: Currency)

    @Delete
    fun delete(currency: Currency?)
}
