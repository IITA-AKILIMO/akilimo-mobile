package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.akilimo.mobile.entities.InvestmentAmount
import kotlinx.coroutines.flow.Flow

@Dao
interface InvestmentAmountDao {

    @Query("SELECT * FROM investment_amounts ORDER BY sort_order ASC")
    fun observeAll(): Flow<List<InvestmentAmount>>

    @Query("SELECT * FROM investment_amounts WHERE country_code = :countryCode AND active = 1 ORDER BY sort_order ASC")
    fun observeAllByCountry(countryCode: String): Flow<List<InvestmentAmount>>


    @Query("SELECT * FROM investment_amounts WHERE id = :id LIMIT 1")
    fun findOne(id: Int): InvestmentAmount?

    @Query("SELECT * FROM investment_amounts WHERE country_code = :countryCode ORDER BY sort_order ASC")
    fun getByCountry(countryCode: String): List<InvestmentAmount>

    @Query("SELECT * FROM investment_amounts WHERE active = 1 ORDER BY sort_order ASC")
    fun getActive(): List<InvestmentAmount>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(item: InvestmentAmount): Long

    @Transaction
    fun upsert(item: InvestmentAmount) {
        val updated = update(item)
        if (updated == 0) {
            insert(item)
        }
    }

    @Update
    fun update(item: InvestmentAmount): Int

    @Delete
    fun delete(item: InvestmentAmount): Int

    @Query("DELETE FROM investment_amounts WHERE id = :id")
    fun deleteById(id: Int): Int


}
