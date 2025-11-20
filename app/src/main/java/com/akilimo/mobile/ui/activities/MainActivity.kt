package com.akilimo.mobile.ui.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.akilimo.mobile.AppDatabase
import com.akilimo.mobile.adapters.FertilizerAdapter
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityMainBinding
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.helper.WorkerError
import com.akilimo.mobile.workers.WorkConstants
import com.google.android.material.snackbar.Snackbar
import io.sentry.Sentry
import io.sentry.SentryLevel
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : BaseActivity<ActivityMainBinding>() {

    private lateinit var adapter: FertilizerAdapter


    override fun inflateBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
    }

    override fun onBindingReady(savedInstanceState: Bundle?) {
        setupWindowInsets()
        setupRecyclerView()
        loadInitialData()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupRecyclerView() {
        adapter = FertilizerAdapter()
        binding.fertilizerRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
            setHasFixedSize(true)
        }
    }

    private fun loadInitialData() {
        lifecycleScope.launch {
            loadFertilizersFromDb()
        }
    }

    private suspend fun loadFertilizersFromDb() {
        try {
            showLoading(true)

            val db = AppDatabase.getDatabase(this)
            val fertilizers = db.fertilizerDao().getAll()

            Timber.d("Loaded ${fertilizers.size} fertilizers from DB")
            Sentry.addBreadcrumb("Loaded ${fertilizers.size} fertilizers from DB", "database")

            updateUI(fertilizers)

        } catch (e: Exception) {
            Timber.e(e, "Failed to load fertilizers from DB")
            Sentry.captureException(e)
            showError("Failed to load fertilizers")
        } finally {
            showLoading(false)
        }
    }

    private fun updateUI(fertilizers: List<Fertilizer>) {
        adapter.submitList(fertilizers)
        showEmptyState(fertilizers.isEmpty())
    }

    override fun observeSyncWorker() {
        WorkManager.getInstance(this)
            .getWorkInfosForUniqueWorkLiveData(WorkConstants.FERTILIZER_WORK_NAME)
            .observe(this) { workInfos ->
                workInfos?.firstOrNull()?.let { workInfo ->
                    handleWorkerState(workInfo)
                }
            }
    }

    private fun handleWorkerState(workInfo: WorkInfo) {
        when (workInfo.state) {
            WorkInfo.State.ENQUEUED -> {
                Timber.d("FertilizerSyncWorker is enqueued")
                showSyncIndicator(true)
            }

            WorkInfo.State.RUNNING -> {
                Timber.d("FertilizerSyncWorker is running")
                showSyncIndicator(true)
            }

            WorkInfo.State.SUCCEEDED -> {
                Timber.d("FertilizerSyncWorker succeeded — refreshing UI")
                Sentry.addBreadcrumb("FertilizerSyncWorker succeeded", "worker")
                showSyncIndicator(false)

                lifecycleScope.launch {
                    loadFertilizersFromDb()
                }
                Snackbar.make(
                    binding.root,
                    "Fertilizers synced successfully",
                    Snackbar.LENGTH_SHORT
                ).show()
            }

            WorkInfo.State.FAILED -> {
                Timber.e("FertilizerSyncWorker failed")
                Sentry.captureMessage("FertilizerSyncWorker failed", SentryLevel.WARNING)
                showSyncIndicator(false)

                WorkerError.showWorkerError(this, workInfo)

                val errorMessage = workInfo.outputData.getString("errorMessage")
                showError(errorMessage ?: "Sync failed")
            }

            WorkInfo.State.BLOCKED -> {
                Timber.d("FertilizerSyncWorker is blocked")
                showSyncIndicator(true)
            }

            WorkInfo.State.CANCELLED -> {
                Timber.d("FertilizerSyncWorker was cancelled")
                showSyncIndicator(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        // Large centered progress bar for initial loading
        binding.progressBar.isVisible = isLoading
    }

    private fun showSyncIndicator(isSyncing: Boolean) {
        // Small top-right indicator for background sync
        binding.syncIndicator.isVisible = isSyncing
    }

    private fun showEmptyState(isEmpty: Boolean) {
        binding.emptyStateView.isVisible = isEmpty
        binding.fertilizerRecyclerView.isVisible = !isEmpty
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Retry") {
                lifecycleScope.launch {
                    loadFertilizersFromDb()
                }
            }
            .show()
    }
}