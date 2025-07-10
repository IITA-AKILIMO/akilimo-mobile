package com.akilimo.mobile.data

import com.akilimo.mobile.dao.AppDatabase

class UserDataCleaner(
    private val database: AppDatabase,
) {

    fun clearUserRelatedData() {
        with(database) {
//            clearAllDao.clearAll()
            clearAllTables()
        }
    }
}