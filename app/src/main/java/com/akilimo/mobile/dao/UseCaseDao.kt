package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.akilimo.mobile.entities.UseCase
import com.akilimo.mobile.entities.UseCaseTask
import com.akilimo.mobile.models.UseCaseWithTasks
import com.akilimo.mobile.utils.enums.EnumUseCase

@Dao
interface UseCaseDao {

    @Query("SELECT * FROM use_cases")
    suspend fun getAllUseCases(useCase: EnumUseCase): List<UseCase>


    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUseCasesWithTasks(data: List<UseCaseWithTasks>) {
        for (useCaseWithTasks in data) {
            val useCaseId = insertUseCase(useCaseWithTasks.useCase)

            val tasks = useCaseWithTasks.useCaseTasks.map { task ->
                task.copy(useCaseId = useCaseId)
            }

            insertTasks(tasks)
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUseCase(useCase: UseCase): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(tasks: UseCaseTask)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<UseCaseTask>)

    @Transaction
    @Query("SELECT * FROM use_cases where use_case = :useCase")
    suspend fun findOne(useCase: EnumUseCase): UseCase?

    @Transaction
    @Query("SELECT * FROM use_cases where use_case = :useCase")
    suspend fun getUseCaseWithTasks(useCase: EnumUseCase): UseCaseWithTasks?

    @Transaction
    @Query("SELECT * FROM use_cases")
    suspend fun getAllUseCasesWithTasks(): List<UseCaseWithTasks>

    @Transaction
    @Query("SELECT * FROM use_case_tasks where use_case_id = :useCaseId")
    suspend fun getAllTasksForUseCase(useCaseId: Long): List<UseCaseTask>

    @Update
    fun updateTaskCompletion(useCaseTask: UseCaseTask): UseCaseTask
}
