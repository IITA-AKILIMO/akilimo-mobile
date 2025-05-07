package com.akilimo.mobile.utils

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.akilimo.mobile.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class InAppUpdate(private val activity: Activity) {

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)
    private val updateType = AppUpdateType.FLEXIBLE

    companion object {
        private const val TAG = "InAppUpdate"
        private const val UPDATE_REQUEST_CODE = 500
        private const val RESULT_CANCELLED = 0
    }

    private val updateStateListener = InstallStateUpdatedListener { state: InstallState ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            showUpdateCompleteSnackBar()
        }
    }

    fun checkForUpdates() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { updateInfo ->
            updateInfo.apply {
                val updateAvailable = updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                val updateAllowed = isUpdateTypeAllowed(updateType)

                if (updateAvailable && updateAllowed) {
                    val updateOptions = AppUpdateOptions.newBuilder(updateType).build()
                    appUpdateManager.startUpdateFlowForResult(
                        updateInfo, activity, updateOptions, UPDATE_REQUEST_CODE
                    )
                }
            }
        }

        appUpdateManager.registerListener(updateStateListener)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int) {
        if (requestCode == UPDATE_REQUEST_CODE) {
            when (resultCode) {
                RESULT_CANCELLED -> {
                    Toast.makeText(activity, R.string.update_cancelled, Toast.LENGTH_SHORT).show()
                }

                AppCompatActivity.RESULT_OK -> {
                    Log.d(TAG, "Update flow completed successfully")
                }

                else -> {
                    // Retry check on unexpected result
                    checkForUpdates()
                }
            }
        }
    }

    private fun showUpdateCompleteSnackBar() {
        Snackbar.make(
            activity.findViewById(R.id.content),
            R.string.update_downloaded,
            Snackbar.LENGTH_INDEFINITE
        ).setAction("RESTART") {
            appUpdateManager.completeUpdate()
        }.show()
    }

    fun onResume() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.installStatus() == InstallStatus.DOWNLOADED) {
                showUpdateCompleteSnackBar()
            }
        }
    }

    fun onDestroy() {
        appUpdateManager.unregisterListener(updateStateListener)
    }
}