package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.akilimo.mobile.entities.SelectedInvestment
import kotlinx.coroutines.flow.Flow

@Dao
interface SelectedInvestmentDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(selected: SelectedInvestment): Long

    @Update
    fun update(selectedInvestment: SelectedInvestment)

    @Upsert
    fun upsert(investment: SelectedInvestment)

    @Query("SELECT * FROM selected_investments WHERE user_id = :userId LIMIT 1")
    fun getSelectedByUser(userId: Int): SelectedInvestment?

    @Query("SELECT * FROM selected_investments WHERE investment_id = :investmentId")
    fun getSelectedByInvestmentId(investmentId: Int): SelectedInvestment?

    @Query("DELETE FROM selected_investments WHERE user_id = :userId and investment_id =:investmentId")
    fun deleteSelected(userId: Int, investmentId: Int)


    @Query("SELECT * FROM selected_investments WHERE user_id = :userId")
    fun observeByUser(userId: Int): Flow<SelectedInvestment?>

    @Query("SELECT COUNT(*) FROM selected_investments WHERE user_id = :userId AND investment_id=:investmentId")
    fun isSelected(userId: Int, investmentId: Int): Int

}
