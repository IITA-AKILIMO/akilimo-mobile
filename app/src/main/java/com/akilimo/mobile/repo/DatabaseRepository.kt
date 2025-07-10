package com.akilimo.mobile.repo

import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.entities.CurrentPractice
import com.akilimo.mobile.entities.FieldOperationCost
import com.akilimo.mobile.entities.UseCaseTask

class DatabaseRepository(private val db: AppDatabase) {

    fun getCurrentPractice(): CurrentPractice? = db.currentPracticeDao().findOne()

    fun getFieldOperationCost(): FieldOperationCost? = db.fieldOperationCostDao().findOne()

    fun getUseCaseTask(useCaseId: Long): UseCaseTask? =
        db.useCaseDao().findOneTask(useCaseId)

    fun saveCurrentPractice(practice: CurrentPractice) {
        db.currentPracticeDao().insert(practice)
    }

    fun saveFieldOperationCost(cost: FieldOperationCost) {
        db.fieldOperationCostDao().insert(cost)
    }

    fun updateUseCaseTask(useCaseTask: UseCaseTask) {
        db.useCaseDao().updateTaskCompletion(useCaseTask = useCaseTask)
    }
}