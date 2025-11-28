package com.akilimo.mobile.base

import androidx.room.ColumnInfo
import androidx.room.Ignore

open class BaseEntity {
    @ColumnInfo(name = "created_at")
    open var createdAt: Long? = null   // nullable, defaults to null

    @ColumnInfo(name = "updated_at")
    open var updatedAt: Long = System.currentTimeMillis()

    @Ignore
    open var isSelected: Boolean = false
}
