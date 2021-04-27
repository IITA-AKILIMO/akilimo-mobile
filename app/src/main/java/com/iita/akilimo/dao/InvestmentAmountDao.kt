package com.iita.akilimo.dao

import androidx.room.*
import com.iita.akilimo.entities.InvestmentAmount

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
}
