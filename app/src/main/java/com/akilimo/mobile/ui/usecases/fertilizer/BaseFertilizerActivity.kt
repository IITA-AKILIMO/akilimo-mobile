package com.akilimo.mobile.ui.usecases.fertilizer

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
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
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.FertilizerPriceRepo
import com.akilimo.mobile.repos.FertilizerRepo
import com.akilimo.mobile.repos.SelectedFertilizerRepo
import com.akilimo.mobile.ui.components.ToolbarHelper
import com.akilimo.mobile.workers.FertilizerWorker
import com.akilimo.mobile.workers.WorkConstants
import com.akilimo.mobile.workers.WorkerScheduler
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Base activity for fertilizer-related screens.
 * Subclasses only need to override [fetchFertilizers] to customize data source.
 */
abstract class BaseFertilizerActivity<VB : ViewBinding> : BaseActivity<VB>() {

    protected lateinit var userRepo: AkilimoUserRepo
    protected lateinit var fertilizerRepo: FertilizerRepo
    protected lateinit var selectedRepo: SelectedFertilizerRepo
    protected lateinit var priceRepo: FertilizerPriceRepo

    protected lateinit var listManager: FertilizerListManager
    protected lateinit var selectionHelper: FertilizerSelectionHelper

    protected val gridSpanCount by lazy { resources.getInteger(R.integer.grid_span_count_default) }

    // ========== Abstract members to override ==========

    /** The advice task type for completion tracking */
    abstract val adviseTask: EnumAdviceTask
    abstract val useCase: EnumUseCase

    /** Toolbar menu resource (null for no menu) */
    open val toolbarMenuRes: Int? = R.menu.menu_fertilizers

    /** Whether layout toggle is enabled */
    open val enableLayoutToggle: Boolean = true

    /** Whether refresh FAB is enabled */
    open val enableRefreshFab: Boolean = true

    // ========== View accessors (override in subclass) ==========

    abstract fun getToolbar(): androidx.appcompat.widget.Toolbar
    abstract fun getRecyclerView(): RecyclerView
    abstract fun getRootView(): View
    abstract fun getEmptyStateView(): View?
    open fun getSyncIndicator(): View? = null
    open fun getRefreshFab(): View? = null

    // ========== Data fetching (THE MAIN OVERRIDE POINT) ==========

    /**
     * Override this to customize how fertilizers are fetched.
     * Default implementation fetches by country.
     */
    open fun fetchFertilizers(country: EnumCountry): Flow<List<Fertilizer>> {
        return fertilizerRepo.observeByCountry(country)
    }

    // ========== Lifecycle ==========

    override fun onBindingReady(savedInstanceState: Bundle?) {
        initRepositories()
        initHelpers()
        setupToolbar()
        setupRecycler()
        setupRefreshFab()
        observeData()
    }

    private fun initRepositories() {
        userRepo = AkilimoUserRepo(database.akilimoUserDao())
        fertilizerRepo = FertilizerRepo(database.fertilizerDao())
        selectedRepo = SelectedFertilizerRepo(database.selectedFertilizerDao())
        priceRepo = FertilizerPriceRepo(database.fertilizerPriceDao())
    }

    private fun initHelpers() {
        listManager = FertilizerListManager(
            recyclerView = getRecyclerView(),
            fertilizerRepo = fertilizerRepo,
            selectedRepo = selectedRepo,
            gridSpanCount = gridSpanCount
        )
        selectionHelper = FertilizerSelectionHelper(
            context = this,
            lifecycleScope = lifecycleScope,
            priceRepo = priceRepo,
            selectedRepo = selectedRepo
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
            onItemClick = { fertilizer -> showPriceDialog(fertilizer) }
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

    private fun observeData() = safeScope.launch {
        val user = userRepo.getUser(sessionManager.akilimoUser) ?: return@launch
        val userId = user.id ?: return@launch
        val country = user.enumCountry

        // Observe fertilizers using the overridable fetchFertilizers method
        launch {
            fetchFertilizers(country).collectLatest { fertilizers ->
                val selectedList = selectedRepo.getSelectedSync(userId)
                val mapped = mapFertilizersWithSelection(fertilizers, selectedList)
                listManager.adapter.submitList(mapped)
                showEmptyState(mapped.isEmpty())
            }
        }

        // Observe selection changes
        launch {
            selectedRepo.observeSelected(userId).collectLatest { selectedList ->
                val selectedIds = selectedList.map { it.fertilizerId }.toSet()
                listManager.adapter.updateSelection(selectedIds)
            }
        }
    }

    private fun mapFertilizersWithSelection(
        fertilizers: List<Fertilizer>,
        selectedList: List<com.akilimo.mobile.entities.SelectedFertilizer>
    ): List<Fertilizer> {
        val selectedIds = selectedList.map { it.fertilizerId }.toSet()
        val selectedMap = selectedList.associateBy { it.fertilizerId }

        return fertilizers.map { fertilizer ->
            fertilizer.apply {
                val selected = selectedMap[id]
                isSelected = selectedIds.contains(id)
                displayPrice = selected?.displayPrice.orEmpty()
                selectedPrice = if (isSelected) selected?.fertilizerPrice ?: 0.0 else 0.0
            }
        }
    }

    private fun showPriceDialog(fertilizer: Fertilizer) = safeScope.launch {
        val user = userRepo.getUser(sessionManager.akilimoUser) ?: return@launch
        val userId = user.id ?: return@launch

        selectionHelper.showPriceDialog(
            fertilizer = fertilizer,
            userId = userId,
            onSelectionChanged = { fertilizerId, price, displayPrice, isSelected ->
                listManager.updateItemSelection(fertilizerId, price, displayPrice, isSelected)
            }
        )
    }

    private fun showEmptyState(isEmpty: Boolean) {
        getEmptyStateView()?.isVisible = isEmpty
        getRecyclerView().isVisible = !isEmpty
    }

    // ========== Sync Worker ==========

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

    // ========== Back Navigation ==========

    override fun handleBackPressed(): Boolean {
        handleBackNavigation()
        return true
    }

    private fun handleBackNavigation() = safeScope.launch {
        val user = userRepo.getUser(sessionManager.akilimoUser) ?: return@launch
        val userId = user.id ?: 0

        val selected = selectedRepo.getSelectedSync(userId)
        val isCompleted = selected.isNotEmpty()

        val completion = AdviceCompletionDto(
            taskName = adviseTask,
            stepStatus = if (isCompleted) EnumStepStatus.COMPLETED else EnumStepStatus.IN_PROGRESS
        )

        val data = Intent().putExtra(COMPLETED_TASK, completion)
        setResult(RESULT_OK, data)
        finish()
    }
}