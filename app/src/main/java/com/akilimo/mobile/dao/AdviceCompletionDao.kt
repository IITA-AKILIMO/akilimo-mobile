package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.akilimo.mobile.entities.AdviceCompletion
import com.akilimo.mobile.enums.EnumAdviceTask
import kotlinx.coroutines.flow.Flow

@Dao
interface AdviceCompletionDao {
    @Query("SELECT * FROM advice_completions")
    fun getAllFlow(): Flow<List<AdviceCompletion>>

    @Query("SELECT * FROM advice_completions WHERE task_name = :taskName")
    fun getAdviceByTask(taskName: EnumAdviceTask): AdviceCompletion?

    @Upsert
    fun upsert(entity: AdviceCompletion)

    @Query("DELETE FROM advice_completions WHERE task_name = :taskName")
    fun delete(taskName: EnumAdviceTask)
}
