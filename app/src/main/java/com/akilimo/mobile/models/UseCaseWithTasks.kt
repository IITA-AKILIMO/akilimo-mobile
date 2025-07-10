package com.akilimo.mobile.models

import androidx.room.Embedded
import androidx.room.Relation
import com.akilimo.mobile.entities.UseCase
import com.akilimo.mobile.entities.UseCaseTask

data class UseCaseWithTasks(
    @Embedded
    val useCase: UseCase,

    @Relation(
        parentColumn = "id",
        entityColumn = "use_case_id"
    )
    val useCaseTasks: List<UseCaseTask>,
)