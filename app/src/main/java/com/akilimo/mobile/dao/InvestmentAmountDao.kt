package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.akilimo.mobile.entities.InvestmentAmount
import com.akilimo.mobile.enums.EnumCountry
import kotlinx.coroutines.flow.Flow

@Dao
interface InvestmentAmountDao {

    @Query("SELECT * FROM investment_amounts ORDER BY sort_order ASC")
    fun observeAll(): Flow<List<InvestmentAmount>>

    @Query("SELECT * FROM investment_amounts WHERE country_code = :countryCode AND active = 1 ORDER BY sort_order ASC")
    fun observeAllByCountry(countryCode: EnumCountry): Flow<List<InvestmentAmount>>


    @Query("SELECT * FROM investment_amounts WHERE id = :id LIMIT 1")
    suspend fun findOne(id: Int): InvestmentAmount?

    @Query("SELECT * FROM investment_amounts WHERE country_code = :countryCode ORDER BY sort_order ASC")
    suspend fun getByCountry(countryCode: EnumCountry): List<InvestmentAmount>

    @Query("SELECT * FROM investment_amounts WHERE active = 1 ORDER BY sort_order ASC")
    suspend fun getActive(): List<InvestmentAmount>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: InvestmentAmount): Long

    @Transaction
    suspend fun upsert(item: InvestmentAmount) {
        val updated = update(item)
        if (updated == 0) {
            insert(item)
        }
    }

    @Update
    suspend fun update(item: InvestmentAmount): Int

    @Delete
    suspend fun delete(item: InvestmentAmount): Int

    @Query("DELETE FROM investment_amounts WHERE id = :id")
    suspend fun deleteById(id: Int): Int


}
