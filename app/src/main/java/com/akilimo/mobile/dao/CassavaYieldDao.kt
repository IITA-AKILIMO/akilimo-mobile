package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akilimo.mobile.entities.CassavaYield
import kotlinx.coroutines.flow.Flow

@Dao
interface CassavaYieldDao {
    @Query("SELECT * FROM cassava_yields ORDER BY id ASC")
    fun observeAll(): Flow<List<CassavaYield>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(items: List<CassavaYield>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(item: CassavaYield): Long

    @Update
    fun update(item: CassavaYield)

//    @Query("UPDATE cassava_yields SET is_selected = CASE WHEN id = :selectedId THEN 1 ELSE 0 END, updated_at = :now WHERE id IN (SELECT id FROM cassava_yields)")
//    suspend fun setSingleSelected(selectedId: Long, now: Long = System.currentTimeMillis())
//
//    @Query("UPDATE cassava_yields SET is_selected = :isSelected, updated_at = :now WHERE id = :id")
//    suspend fun updateSelection(
//        id: Long, isSelected: Boolean, now: Long = System.currentTimeMillis()
//    )

    @Query("DELETE FROM cassava_yields")
    fun clearAll()
}
