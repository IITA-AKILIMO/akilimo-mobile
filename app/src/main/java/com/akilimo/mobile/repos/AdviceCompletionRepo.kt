package com.akilimo.mobile.repos

import com.akilimo.mobile.dao.AdviceCompletionDao
import com.akilimo.mobile.entities.AdviceCompletion
import com.akilimo.mobile.entities.AdviceCompletionDto
import com.akilimo.mobile.enums.EnumAdviceTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date

class AdviceCompletionRepo(private val dao: AdviceCompletionDao) {

    fun getAllCompletions(): Flow<Map<String, AdviceCompletion>> =
        dao.getAllFlow().map { list -> list.associateBy { it.taskName.name } }
    suspend fun updateStatus(advice: AdviceCompletionDto) {
//        val existing = dao.getAdviceByTask(advice.taskName)
        val entity = AdviceCompletion(
            taskName = advice.taskName,
            stepStatus = advice.stepStatus,
            completedAtMillis = Date().time
        )
        dao.upsert(entity)

    }

    suspend fun clearCompleted(task: EnumAdviceTask) {
        dao.delete(task)
    }
}
