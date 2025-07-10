package com.akilimo.mobile.repo

import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.entities.CurrentPractice
import com.akilimo.mobile.entities.FieldOperationCost
import com.akilimo.mobile.entities.UseCaseTask

class DatabaseRepository(private val db: AppDatabase) {

    suspend fun getCurrentPractice(): CurrentPractice? = db.currentPracticeDao().findOne()

    suspend fun getFieldOperationCost(): FieldOperationCost? = db.fieldOperationCostDao().findOne()

    suspend fun getUseCaseTask(useCaseId: Long): UseCaseTask? =
        db.useCaseDao().findOneTask(useCaseId)

    suspend fun saveCurrentPractice(practice: CurrentPractice) {
        db.currentPracticeDao().insert(practice)
    }

    suspend fun saveFieldOperationCost(cost: FieldOperationCost) {
        db.fieldOperationCostDao().insert(cost)
    }

    suspend fun updateUseCaseTask(useCaseTask: UseCaseTask): UseCaseTask {
        return db.useCaseDao().updateTaskCompletion(useCaseTask = useCaseTask)
    }
}