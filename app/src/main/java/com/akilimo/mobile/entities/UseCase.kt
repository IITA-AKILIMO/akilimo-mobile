package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.akilimo.mobile.utils.enums.EnumTask
import com.akilimo.mobile.utils.enums.EnumUseCase

// Parent entity
@Entity(
    tableName = "use_cases",
    indices = [
        Index(value = ["use_case_id"]),
        Index(value = ["use_case"], unique = true)
    ]
)
data class UseCase(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id", index = true)
    val id: Long = 0L,

    @ColumnInfo(name = "use_case_label")
    val useCaseLabel: Int = -1,

    @ColumnInfo(name = "use_case", index = true)
    val useCase: EnumUseCase = EnumUseCase.NA
)

// Child entity with FK to UseCase
@Entity(
    tableName = "use_case_tasks",
    foreignKeys = [ForeignKey(
        entity = UseCase::class,
        parentColumns = ["id"],
        childColumns = ["use_case_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index(value = ["use_case_id"]),
        Index(value = ["use_case_id", "task_name"], unique = true)
    ]

)
data class UseCaseTask(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id", index = true)
    val id: Long = 0L,

    @ColumnInfo(name = "use_case_id")
    val useCaseId: Long = -1,

    @ColumnInfo(name = "task_label")
    val taskLabel: Int = -1,

    @ColumnInfo(name = "task_name")
    val taskName: EnumTask = EnumTask.NOT_SELECTED,

    @ColumnInfo(name = "completed")
    val completed: Boolean = false
)
