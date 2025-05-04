package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akilimo.mobile.entities.InvestmentAmount

@Dao
interface InvestmentAmountDao {

    @Query("SELECT * FROM investment_amounts")
    fun listAll(): List<InvestmentAmount>

    @Query("SELECT * FROM investment_amounts LIMIT 1")
    fun findOne(): InvestmentAmount?

    @Query("SELECT * FROM investment_amounts where item_tag=:itemTag LIMIT 1")
    fun findOneByItemTag(itemTag: String): InvestmentAmount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg investmentAmount: InvestmentAmount)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(investmentList: List<InvestmentAmount>): LongArray?

    @Update
    fun update(vararg investmentAmount: InvestmentAmount?)

    @Delete
    fun delete(investmentAmount: InvestmentAmount?)

    @Query("DELETE FROM investment_amounts")
    fun deleteAll()
}
