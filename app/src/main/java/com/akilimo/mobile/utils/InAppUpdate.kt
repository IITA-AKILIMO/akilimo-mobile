package com.akilimo.mobile.utils

import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.akilimo.mobile.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
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

    private val updateStateListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            showUpdateDownloadedSnackBar()
        }
    }

    fun checkForUpdates() {
        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { updateInfo ->
                val updateAvailable =
                    updateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                val updateAllowed = updateInfo.isUpdateTypeAllowed(updateType)

                if (updateAvailable && updateAllowed) {
                    val options = AppUpdateOptions.newBuilder(updateType).build()
                    appUpdateManager.startUpdateFlowForResult(
                        updateInfo,
                        activity,
                        options,
                        UPDATE_REQUEST_CODE
                    )
                }
            }

        appUpdateManager.registerListener(updateStateListener)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int) {
        if (requestCode != UPDATE_REQUEST_CODE) return

        when (resultCode) {
            RESULT_CANCELLED -> {
                Toast.makeText(activity, R.string.update_cancelled, Toast.LENGTH_SHORT).show()
            }

            AppCompatActivity.RESULT_OK -> {
                Log.d(TAG, "Update flow completed successfully")
            }

            else -> {
                // Retry on unexpected result
                Log.w(TAG, "Unexpected update result: $resultCode, retrying update check")
                checkForUpdates()
            }
        }
    }

    fun onResume() {
        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { info ->
                if (info.installStatus() == InstallStatus.DOWNLOADED) {
                    showUpdateDownloadedSnackBar()
                }
            }
    }

    fun onDestroy() {
        appUpdateManager.unregisterListener(updateStateListener)
    }

    private fun showUpdateDownloadedSnackBar() {
        val rootView: View? = activity.findViewById(android.R.id.content)
        rootView?.let {
            Snackbar.make(it, R.string.update_downloaded, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.restart) {
                    appUpdateManager.completeUpdate()
                }
                .show()
        } ?: run {
            Log.e(TAG, "Failed to show snackbar: root view is null")
        }
    }
}
