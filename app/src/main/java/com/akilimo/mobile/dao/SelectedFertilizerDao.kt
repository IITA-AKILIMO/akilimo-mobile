package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.akilimo.mobile.entities.SelectedFertilizer
import kotlinx.coroutines.flow.Flow

@Dao
interface SelectedFertilizerDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(selected: SelectedFertilizer): Long

    @Update
    fun update(selectedFertilizer: SelectedFertilizer)

    @Upsert
    fun upsert(selectedFertilizer: SelectedFertilizer)

    @Query("SELECT * FROM selected_fertilizers WHERE user_id = :userId")
    fun getSelectedByUser(userId: Int): List<SelectedFertilizer>

    @Query("SELECT * FROM selected_fertilizers WHERE fertilizer_id = :fertilizerId")
    fun getSelectedByFertilizer(fertilizerId: Int): SelectedFertilizer?

    @Query("DELETE FROM selected_fertilizers WHERE user_id = :userId and fertilizer_id = :fertilizerId")
    fun deleteSelected(userId: Int, fertilizerId: Int)


    @Query("SELECT * FROM selected_fertilizers WHERE user_id = :userId")
    fun observeByUser(userId: Int): Flow<List<SelectedFertilizer>>

    @Query("SELECT COUNT(*) FROM selected_fertilizers WHERE user_id = :userId AND fertilizer_id = :fertilizerId")
    fun isSelected(userId: Int, fertilizerId: Int): Int

    @Query("DELETE FROM selected_fertilizers WHERE user_id = :userId")
    fun deleteAllByUser(userId: Int)
}
