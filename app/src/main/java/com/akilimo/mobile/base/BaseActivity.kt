package com.akilimo.mobile.base

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.akilimo.mobile.AkilimoApp
import com.akilimo.mobile.AppDatabase
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.helper.LocaleHelper
import com.akilimo.mobile.interfaces.DefaultDispatcherProvider
import javax.inject.Inject
import com.akilimo.mobile.interfaces.IDispatcherProvider
import com.akilimo.mobile.ui.components.NetworkNotificationView
import com.akilimo.mobile.utils.PermissionHelper
import kotlinx.coroutines.launch

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    companion object {
        const val COMPLETED_TASK = "completed_task"
    }

    @Inject lateinit var appSettings: AppSettingsDataStore

    /** Backward-compatible alias — all existing call sites keep working unchanged. */
    protected val sessionManager get() = appSettings

    protected val dispatcherProvider: IDispatcherProvider = DefaultDispatcherProvider()

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

    override fun attachBaseContext(newBase: Context) {
        val langTag = AppSettingsDataStore.readLanguageTagSync(newBase)
        val wrapped = LocaleHelper.wrap(newBase, langTag)
        super.attachBaseContext(wrapped)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
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
        injectNetworkBannerIfNeeded()
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

    /**
     * Programmatically add the network banner to every activity that hasn't already
     * wired one via its layout XML. The banner is added to the window's content FrameLayout
     * (android.R.id.content) so it overlays all content at the top of the screen without
     * requiring each layout to declare it. A window-inset listener offsets it below the
     * status bar so it is always fully visible in edge-to-edge mode.
     *
     * Activities that explicitly assign networkNotificationView in onBindingReady()
     * (e.g. HomeStepperActivity which has it in its XML layout) are skipped.
     */
    private fun injectNetworkBannerIfNeeded() {
        if (networkNotificationView != null) return

        val contentFrame = window.decorView.findViewById<FrameLayout>(android.R.id.content)
        val banner = NetworkNotificationView(this)
        val lp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.TOP
        )
        contentFrame.addView(banner, lp)

        ViewCompat.setOnApplyWindowInsetsListener(banner) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(0, statusBarHeight, 0, 0)
            insets
        }

        networkNotificationView = banner
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