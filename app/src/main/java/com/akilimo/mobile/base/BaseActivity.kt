package com.akilimo.mobile.base

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
import kotlinx.coroutines.launch


abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE_LOCATION_PERMISSION = 1001
        const val COMPLETED_TASK = "completed_task"
    }

    protected val dispatcherProvider: IDispatcherProvider = DefaultDispatcherProvider()

    protected val sessionManager: SessionManager by lazy { SessionManager(this@BaseActivity) }
    protected val database: AppDatabase by lazy { AppDatabase.getDatabase(this@BaseActivity) }

    protected var networkNotificationView: NetworkNotificationView? = null
    private var wasConnected: Boolean? = null


    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

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
        if (!hasLocationPermissions()) {
            ActivityCompat.requestPermissions(
                this,
                locationPermissions,
                REQUEST_CODE_LOCATION_PERMISSION
            )
        }
        _binding = inflateBinding()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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
    }

    protected fun openActivity(intent: Intent?) {
        intent?.let {
            startActivity(it)
//            Animatoo.animateSlideRight(this@BaseActivity)
        }
    }

    /**
     * Observe LiveData, Flow, or StateFlow from ViewModels
     * Called once during onCreate after setupView.
     */
    protected open fun observeSyncWorker() {
        // Override in subclasses
    }

    private fun hasLocationPermissions(): Boolean {
        return locationPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1001) {
            val granted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (!granted) {
                Toast.makeText(this, "Location permissions are required", Toast.LENGTH_LONG).show()
            }
        }
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