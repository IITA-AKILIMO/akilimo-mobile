package com.iita.akilimo.dao

import androidx.room.*
import com.iita.akilimo.entities.UseCaseStepStatus

@Dao
interface UseCaseStepStatusDao {

    @Query("SELECT * FROM use_case_step_status")
    fun listAll(): List<UseCaseStepStatus>

    @Query("SELECT * FROM use_case_step_status where step_name=:stepName")
    fun findOne(stepName: String): UseCaseStepStatus?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(stepStatus: UseCaseStepStatus)

    @Update
    fun update(stepStatus: UseCaseStepStatus)

    @Delete
    fun delete(stepStatus: UseCaseStepStatus?)

    @Query("DELETE FROM use_case_step_status")
    fun deleteAll()
}