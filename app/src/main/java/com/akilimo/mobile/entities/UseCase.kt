package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.akilimo.mobile.utils.enums.EnumTask
import com.akilimo.mobile.utils.enums.EnumUseCase
@Entity(
    tableName = "use_cases",
    indices = [Index(value = ["use_case"], unique = true)]
)
data class UseCase(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0L,

    @ColumnInfo(name = "use_case_label")
    var useCaseLabel: Int = -1, // Consider renaming to labelResId

    @ColumnInfo(name = "use_case")
    var useCase: EnumUseCase = EnumUseCase.NA
)

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
    @ColumnInfo(name = "id")
    var id: Long = 0L,

    @ColumnInfo(name = "use_case_id")
    var useCaseId: Long = -1,

    @ColumnInfo(name = "task_label")
    var taskLabel: Int = -1, // Consider renaming to labelResId

    @ColumnInfo(name = "task_name")
    var taskName: EnumTask = EnumTask.NOT_SELECTED,

    @ColumnInfo(name = "completed")
    var completed: Boolean = false
)
