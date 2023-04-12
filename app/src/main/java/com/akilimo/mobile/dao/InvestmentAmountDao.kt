package com.akilimo.mobile.dao

import androidx.room.*
import com.akilimo.mobile.entities.InvestmentAmount

@Dao
interface InvestmentAmountDao {

    @Query("SELECT * FROM investment_amount")
    fun listAll(): List<InvestmentAmount>

    @Query("SELECT * FROM investment_amount LIMIT 1")
    fun findOne(): InvestmentAmount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg investmentAmount: InvestmentAmount)

    @Update
    fun update(vararg investmentAmount: InvestmentAmount?)

    @Delete
    fun delete(investmentAmount: InvestmentAmount?)

    @Query("DELETE FROM investment_amount")
    fun deleteAll()
}
