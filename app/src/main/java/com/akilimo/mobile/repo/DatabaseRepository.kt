package com.akilimo.mobile.repo

import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.entities.CurrentPractice
import com.akilimo.mobile.entities.FieldOperationCost

class DatabaseRepository(private val db: AppDatabase) {

    fun getCurrentPractice(): CurrentPractice? = db.currentPracticeDao().findOne()

    fun getFieldOperationCost(): FieldOperationCost? = db.fieldOperationCostDao().findOne()

    fun saveCurrentPractice(practice: CurrentPractice) {
        db.currentPracticeDao().insert(practice)
    }

    fun saveFieldOperationCost(cost: FieldOperationCost) {
        db.fieldOperationCostDao().insertOrUpdate(cost)
    }

    fun saveAdviceStatus(status: AdviceStatus) {
        db.adviceStatusDao().insert(status)
    }
}