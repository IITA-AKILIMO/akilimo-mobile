package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akilimo.mobile.entities.MaizePerformance
import kotlinx.coroutines.flow.Flow

@Dao
interface MaizePerformanceDao {


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(performance: MaizePerformance): Long

    @Update
    suspend fun update(performance: MaizePerformance)

    @Query("SELECT * FROM maize_performance WHERE user_id = :userId LIMIT 1")
    suspend fun getByUserId(userId: Int): MaizePerformance?

    @Query("SELECT * FROM maize_performance WHERE user_id = :userId LIMIT 1")
    fun observeByUserId(userId: Int): Flow<MaizePerformance?>

    @Query("DELETE FROM maize_performance WHERE user_id = :userId")
    suspend fun deleteByUserId(userId: Int)
}

