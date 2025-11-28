package com.akilimo.mobile.utils

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.akilimo.mobile.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("LogNotTimber")
object StartupManager {

    fun runHousekeeping(context: Context) {
        CoroutineScope(Dispatchers.Default).launch {
//            clearRoomDatabase(context)
            cleanCache(context)
            Log.d("StartupManager", "Housekeeping tasks completed")
        }
    }

    private fun clearRoomDatabase(context: Context) {
        val db = AppDatabase.getDatabase(context)
        db.clearAllTables()
        Log.d("StartupManager", "Room database cleared")
    }

    private fun cleanCache(context: Context) {
        val success = context.cacheDir.deleteRecursively()
        Log.d("StartupManager", "Cache cleared: $success")
    }
}