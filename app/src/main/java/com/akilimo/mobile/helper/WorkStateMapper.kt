package com.akilimo.mobile.helper

import android.content.Context
import com.akilimo.mobile.R
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import androidx.work.WorkInfo
import androidx.work.WorkManager

object WorkStateMapper {

    fun observeWorkAsStatus(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        uniqueWorkName: String,
        callback: (WorkStatus) -> Unit
    ) {
        WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkLiveData(uniqueWorkName)
            .observe(lifecycleOwner) { infos ->
                val status =
                    infos?.firstOrNull()?.let { mapWorkInfoToStatus(context, it) } ?: WorkStatus.Enqueued
                callback(status)
            }
    }

    private fun mapWorkInfoToStatus(context: Context, info: WorkInfo): WorkStatus =
        when (info.state) {
            WorkInfo.State.ENQUEUED -> WorkStatus.Enqueued
            WorkInfo.State.RUNNING -> WorkStatus.Running
            WorkInfo.State.SUCCEEDED -> {
                val savedCount = info.outputData.getInt("savedCount", -1)
                WorkStatus.Success(
                    WorkStatus.Payload.Localized(
                        R.string.msg_items_processed,
                        arrayOf(savedCount)
                    )
                )
            }

            WorkInfo.State.FAILED -> {
                val errorMsg = info.outputData.getString("errorMessage")
                val errorCode = info.outputData.getString("errorCode")
                WorkStatus.Failed(
                    WorkStatus.Payload.Raw(
                        errorMsg ?: if (errorCode != null) {
                            context.getString(R.string.msg_operation_failed_with_code, errorCode)
                        } else {
                            context.getString(R.string.msg_operation_failed)
                        }
                    )
                )
            }

            WorkInfo.State.BLOCKED -> WorkStatus.Blocked
            WorkInfo.State.CANCELLED -> WorkStatus.Cancelled
        }
}


sealed class WorkStatus {
    object Running : WorkStatus()
    object Enqueued : WorkStatus()
    object Blocked : WorkStatus()
    object Cancelled : WorkStatus()

    data class Success(val payload: Payload) : WorkStatus()
    data class Failed(val payload: Payload) : WorkStatus()

    sealed class Payload {
        data class Raw(val message: String) : Payload()

        @Suppress("ArrayInDataClass")
        data class Localized(
            @param:StringRes val resId: Int,
            val args: Array<Any> = emptyArray()
        ) : Payload()
    }
}
