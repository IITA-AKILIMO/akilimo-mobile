package com.akilimo.mobile.base

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.akilimo.mobile.AkilimoApp
import com.akilimo.mobile.AppDatabase
import com.akilimo.mobile.R
import com.akilimo.mobile.helper.SessionManager
import com.akilimo.mobile.interfaces.DefaultDispatcherProvider
import com.akilimo.mobile.interfaces.IDispatcherProvider
import com.akilimo.mobile.ui.components.NetworkNotificationView
import com.akilimo.mobile.utils.PermissionHelper
import kotlinx.coroutines.launch

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE_LOCATION_PERMISSION = 1001
        const val COMPLETED_TASK = "completed_task"
    }

    protected val dispatcherProvider: IDispatcherProvider = DefaultDispatcherProvider()
    protected val sessionManager: SessionManager by lazy { SessionManager(this@BaseActivity) }
    protected val database: AppDatabase by lazy { AppDatabase.getDatabase(this@BaseActivity) }
    protected lateinit var permissionHelper: PermissionHelper

    protected var networkNotificationView: NetworkNotificationView? = null
    private var wasConnected: Boolean? = null

    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    // Modern permission launcher for location permissions
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        handleLocationPermissionResult(permissions)
    }

    // ✅ Lifecycle-safe coroutine scope tied to view
    protected val safeScope get() = lifecycleScope

    private var _binding: VB? = null
    protected val binding
        get() = _binding ?: throw IllegalStateException("Binding is not initialized yet.")

    /**
     * Override this in child classes to handle back press.
     * Return true if handled, false to allow default behavior.
     */
    protected open fun handleBackPressed(): Boolean = false

    /**
     * Subclasses must provide their ViewBinding instance.
     */
    protected abstract fun inflateBinding(): VB

    /**
     * Setup views, click listeners, RecyclerView adapters, etc.
     * Called once during onCreate.
     */
    protected abstract fun onBindingReady(savedInstanceState: Bundle?)

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        initializePermissionHelper()

        _binding = inflateBinding()
        setContentView(binding.root)

        // Setup back press dispatcher
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!handleBackPressed()) {
                    finish() // fallback if not handled
                }
            }
        })

        onBindingReady(savedInstanceState)
        observeSyncWorker()
        observeNetworkChanges()
        checkLocationPermissions()
    }

    private fun initializePermissionHelper() {
        permissionHelper = PermissionHelper()
    }

    private fun checkLocationPermissions() {
        if (!permissionHelper.hasLocationPermission(this)) {
            requestLocationPermissionIfNeeded()
        }
    }

    protected fun requestLocationPermissionIfNeeded() {
        if (permissionHelper.hasLocationPermission(this)) {
            return
        }

        if (shouldShowLocationPermissionRationale()) {
            showLocationPermissionRationale()
        } else {
            requestLocationPermission()
        }
    }

    protected fun requestLocationPermission() {
        locationPermissionLauncher.launch(locationPermissions)
    }

    protected fun shouldShowLocationPermissionRationale(): Boolean {
        return permissionHelper.shouldShowPermissionRationale(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) || permissionHelper.shouldShowPermissionRationale(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    protected fun showLocationPermissionRationale() {
        // Show a custom rationale dialog or use a Snackbar
        // For now, just request the permission
        requestLocationPermission()
    }

    private fun handleLocationPermissionResult(permissions: Map<String, Boolean>) {
        val allGranted = permissions.all { it.value }

        if (!allGranted) {
            onLocationPermissionDenied()
        } else {
            onLocationPermissionGranted()
        }
    }

    /**
     * Override this method to handle when location permissions are granted
     */
    protected open fun onLocationPermissionGranted() {
        // Override in child activities if needed
    }

    /**
     * Override this method to handle when location permissions are denied
     */
    protected open fun onLocationPermissionDenied() {
        Toast.makeText(this, "Location permissions are required", Toast.LENGTH_LONG).show()
    }

    /**
     * Check if all required location permissions are granted
     */
    protected fun hasLocationPermissions(): Boolean {
        return permissionHelper.hasLocationPermission(this)
    }

    /**
     * Check if fine location permission is granted
     */
    protected fun hasFineLocationPermission(): Boolean {
        return permissionHelper.hasFineLocationPermission(this)
    }

    /**
     * Check if coarse location permission is granted
     */
    protected fun hasCoarseLocationPermission(): Boolean {
        return permissionHelper.hasCoarseLocationPermission(this)
    }

    /**
     * Check if background location permission is granted (Android 10+)
     */
    protected fun hasBackgroundLocationPermission(): Boolean {
        return permissionHelper.isBackgroundLocationPermissionGranted(this)
    }

    protected fun openActivity(intent: Intent?) {
        intent?.let {
            startActivity(it)
        }
    }

    /**
     * Observe LiveData, Flow, or StateFlow from ViewModels
     * Called once during onCreate after setupView.
     */
    protected open fun observeSyncWorker() {
        // Override in subclasses
    }

    private fun observeNetworkChanges() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                AkilimoApp.instance.networkMonitor.isConnected.collect { isConnected ->
                    handleNetworkChange(isConnected)
                }
            }
        }
    }

    private fun handleNetworkChange(isConnected: Boolean) {
        networkNotificationView?.let { banner ->
            when {
                !isConnected -> {
                    banner.showNoConnection()
                    onNetworkDisconnected()
                }

                isConnected && wasConnected == false -> {
                    banner.showConnected()
                    onNetworkReconnected()
                }
            }
        }
        wasConnected = isConnected
    }

    /**
     * Override this method to handle network disconnection
     */
    protected open fun onNetworkDisconnected() {
        // Override in child activities if needed
    }

    /**
     * Override this method to handle network reconnection
     */
    protected open fun onNetworkReconnected() {
        // Override in child activities if needed
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}