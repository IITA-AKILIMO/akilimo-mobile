package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface ClearAllDao {
    @Query("DELETE FROM use_case_tasks")
    fun clearUseCaseTasks()

    @Query("DELETE FROM use_cases")
    fun clearUseCases()

    fun clearAll() {
        // Call in proper order to maintain referential integrity
        clearUseCaseTasks()
        clearUseCases()
    }
}