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
    fun insert(performance: MaizePerformance): Long

    @Update
    fun update(performance: MaizePerformance)

    @Query("SELECT * FROM maize_performance WHERE user_id = :userId LIMIT 1")
    fun getByUserId(userId: Int): MaizePerformance?

    @Query("SELECT * FROM maize_performance WHERE user_id = :userId LIMIT 1")
    fun observeByUserId(userId: Int): Flow<MaizePerformance?>

    @Query("DELETE FROM maize_performance WHERE user_id = :userId")
    fun deleteByUserId(userId: Int)
}

class MaizePerformanceRepo(
    private val dao: MaizePerformanceDao
) {
    suspend fun saveOrUpdatePerformance(entry: MaizePerformance) {
        val existing = dao.getByUserId(entry.userId)
        if (existing != null) {
            val updated = existing.copy(
                maizePerformance = entry.maizePerformance
            ).apply {
                createdAt = existing.createdAt
                updatedAt = System.currentTimeMillis()
            }
            dao.update(updated)
        } else {
            dao.insert(entry)
        }
    }

    suspend fun getPerformanceForUser(userId: Int): MaizePerformance? {
        return dao.getByUserId(userId)
    }

    fun observeByUserId(userId: Int): Flow<MaizePerformance?> {
        return dao.observeByUserId(userId)
    }

    suspend fun deletePerformanceForUser(userId: Int) {
        dao.deleteByUserId(userId)
    }
}