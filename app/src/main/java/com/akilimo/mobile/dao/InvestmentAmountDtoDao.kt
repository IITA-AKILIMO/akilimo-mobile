package com.akilimo.mobile.dao

import androidx.room.*
import com.akilimo.mobile.entities.FertilizerPrice
import com.akilimo.mobile.entities.InvestmentAmount
import com.akilimo.mobile.entities.InvestmentAmountDto

@Dao
interface InvestmentAmountDtoDao {

    @Query("SELECT * FROM investment_amount_dto")
    fun listAll(): List<InvestmentAmountDto>

    @Query("SELECT * FROM investment_amount_dto LIMIT 1")
    fun findOne(): InvestmentAmountDto?

    @Query("SELECT * FROM investment_amount_dto where investmentId=:itemTagIndex LIMIT 1")
    fun findOneByInvestmentId(itemTagIndex: Long): InvestmentAmountDto?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg investmentAmount: InvestmentAmountDto)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(investmentList: List<InvestmentAmountDto>): LongArray?

    @Update
    fun update(vararg investmentAmount: InvestmentAmountDto?)

    @Delete
    fun delete(investmentAmount: InvestmentAmountDto?)

    @Query("DELETE FROM investment_amount_dto")
    fun deleteAll()
}
