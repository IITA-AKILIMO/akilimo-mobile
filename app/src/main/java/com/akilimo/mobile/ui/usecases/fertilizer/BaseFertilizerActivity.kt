package com.akilimo.mobile.ui.usecases.fertilizer

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.akilimo.mobile.R
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.entities.AdviceCompletionDto
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.enums.EnumUseCase
import com.akilimo.mobile.ui.components.ToolbarHelper
import com.akilimo.mobile.ui.viewmodels.FertilizerViewModel
import com.akilimo.mobile.workers.FertilizerWorker
import com.akilimo.mobile.workers.WorkConstants
import com.akilimo.mobile.workers.WorkerScheduler
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

abstract class BaseFertilizerActivity<VB : ViewBinding> : BaseActivity<VB>() {

    protected val gridSpanCount by lazy { resources.getInteger(R.integer.grid_span_count_default) }

    abstract val adviseTask: EnumAdviceTask
    abstract val useCase: EnumUseCase

    open val toolbarMenuRes: Int? = R.menu.menu_fertilizers
    open val enableLayoutToggle: Boolean = true
    open val enableRefreshFab: Boolean = true

    abstract fun getToolbar(): androidx.appcompat.widget.Toolbar
    abstract fun getRecyclerView(): RecyclerView
    abstract fun getRootView(): View
    abstract fun getEmptyStateView(): View?
    open fun getSyncIndicator(): View? = null
    open fun getRefreshFab(): View? = null

    /** Override to supply a custom fertilizer flow (e.g. filtered by type). */
    open fun fetchFertilizersFlow(country: EnumCountry): Flow<List<Fertilizer>>? = null

    protected lateinit var listManager: FertilizerListManager
    protected lateinit var selectionHelper: FertilizerSelectionHelper

    protected val viewModel: FertilizerViewModel by lazy {
        ViewModelProvider(
            this,
            FertilizerViewModel.factory(
                db = database,
                userName = sessionManager.akilimoUser,
                fetchFlow = fetchFertilizersFlow(EnumCountry.Unsupported)
                    ?.let { flow -> { _: EnumCountry -> flow } }
                    ?: { country -> FertilizerViewModel.defaultFlow(database, country) }
            )
        )[FertilizerViewModel::class.java]
    }

    override fun onBindingReady(savedInstanceState: Bundle?) {
        initHelpers()
        setupToolbar()
        setupRecycler()
        setupRefreshFab()
        observeViewModel()
    }

    private fun initHelpers() {
        listManager = FertilizerListManager(
            recyclerView = getRecyclerView(),
            gridSpanCount = gridSpanCount
        )
        selectionHelper = FertilizerSelectionHelper(
            lifecycleScope = lifecycleScope,
            priceRepo = viewModel.priceRepo,
            selectedRepo = viewModel.selectedRepo
        )
    }

    private fun setupToolbar() {
        val helper = ToolbarHelper(this, getToolbar())
            .showBackButton(true)
            .onNavigationClick { handleBackNavigation() }

        toolbarMenuRes?.let { menuRes ->
            helper.inflateMenu(menuRes) { item -> onToolbarMenuItemClicked(item) }
        }
        helper.build()
    }

    protected open fun onToolbarMenuItemClicked(item: MenuItem) {
        if (enableLayoutToggle && item.itemId == R.id.action_toggle_layout) {
            val isGrid = listManager.toggleLayout()
            sessionManager.isFertilizerGrid = isGrid
            item.setIcon(if (isGrid) R.drawable.ic_list else R.drawable.ic_grid)
        }
    }

    private fun setupRecycler() {
        listManager.initialize(
            initialGridMode = sessionManager.isFertilizerGrid,
            onItemClick = { fertilizer -> showPriceBottomSheet(fertilizer) }
        )
    }

    private fun setupRefreshFab() {
        if (!enableRefreshFab) return
        getRefreshFab()?.setOnClickListener {
            it.isEnabled = false
            WorkerScheduler.scheduleOneTimeWorker<FertilizerWorker>(
                context = this,
                workName = WorkConstants.FERTILIZER_WORK_NAME
            )
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    listManager.adapter.submitList(state.fertilizers)
                    listManager.adapter.updateSelection(state.selectedIds)
                    showEmptyState(state.isEmpty)
                }
            }
        }
    }

    private fun showPriceBottomSheet(fertilizer: Fertilizer) {
        selectionHelper.showPriceBottomSheet(
            fertilizer = fertilizer,
            userId = viewModel.uiState.value.userId,
            fragmentManager = supportFragmentManager,
            onSelectionChanged = { fertilizerId, fertilizerPriceId, price, displayPrice, isSelected, isExactPrice ->
                listManager.updateItemSelection(fertilizerId, price, displayPrice, isSelected)
                if (isSelected) {
                    viewModel.selectFertilizer(fertilizerId, fertilizerPriceId, price, displayPrice, isExactPrice)
                } else {
                    viewModel.deselectFertilizer(fertilizerId)
                }
            }
        )
    }

    private fun showEmptyState(isEmpty: Boolean) {
        getEmptyStateView()?.isVisible = isEmpty
        getRecyclerView().isVisible = !isEmpty
    }

    override fun observeSyncWorker() {
        WorkManager.getInstance(this)
            .getWorkInfosForUniqueWorkLiveData(WorkConstants.FERTILIZER_WORK_NAME)
            .observe(this) { infos ->
                infos?.firstOrNull()?.let { handleWorkerState(it) }
            }
    }

    private fun handleWorkerState(workInfo: WorkInfo) {
        when (workInfo.state) {
            WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING -> showSyncIndicator(true)
            WorkInfo.State.SUCCEEDED -> {
                showSyncIndicator(false)
                getRefreshFab()?.isEnabled = true
                val saved = workInfo.outputData.getInt("savedCount", 0)
                Snackbar.make(
                    getRootView(),
                    getString(R.string.fertilizers_synced, saved),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            else -> {
                showSyncIndicator(false)
                getRefreshFab()?.isEnabled = true
            }
        }
    }

    private fun showSyncIndicator(visible: Boolean) {
        getSyncIndicator()?.isVisible = visible
    }

    override fun handleBackPressed(): Boolean {
        handleBackNavigation()
        return true
    }

    private fun handleBackNavigation() {
        safeScope.launch {
            val status = viewModel.getStepStatus()
            val completion = AdviceCompletionDto(taskName = adviseTask, stepStatus = status)
            val data = Intent().putExtra(COMPLETED_TASK, completion)
            setResult(RESULT_OK, data)
            finish()
        }
    }
}
