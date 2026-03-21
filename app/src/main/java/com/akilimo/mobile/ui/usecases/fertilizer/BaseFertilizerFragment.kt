package com.akilimo.mobile.ui.usecases.fertilizer

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.akilimo.mobile.R
import com.akilimo.mobile.base.BaseFragment
import com.akilimo.mobile.entities.AdviceCompletionDto
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumFertilizerFlow
import com.akilimo.mobile.ui.components.ToolbarHelper
import com.akilimo.mobile.ui.fragments.usecases.UseCaseResults
import com.akilimo.mobile.ui.viewmodels.FertilizerViewModel
import com.akilimo.mobile.workers.FertilizerWorker
import com.akilimo.mobile.workers.WorkConstants
import com.akilimo.mobile.workers.WorkerScheduler
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.lifecycle.withCreationCallback
import kotlinx.coroutines.launch

abstract class BaseFertilizerFragment<VB : ViewBinding> : BaseFragment<VB>() {

    protected val gridSpanCount by lazy { resources.getInteger(R.integer.grid_span_count_default) }

    abstract val adviseTask: EnumAdviceTask
    open val toolbarMenuRes: Int? = R.menu.menu_fertilizers
    open val enableLayoutToggle: Boolean = true
    open val enableRefreshFab: Boolean = true
    open val fertilizerFlow: EnumFertilizerFlow get() = EnumFertilizerFlow.DEFAULT

    abstract fun getToolbar(): androidx.appcompat.widget.Toolbar
    abstract fun getRecyclerView(): RecyclerView
    abstract fun getRootView(): View
    abstract fun getEmptyStateView(): View?
    open fun getSyncIndicator(): View? = null
    open fun getRefreshFab(): View? = null

    protected lateinit var listManager: FertilizerListManager
    protected lateinit var selectionHelper: FertilizerSelectionHelper

    protected val viewModel: FertilizerViewModel by viewModels(
        extrasProducer = {
            defaultViewModelCreationExtras.withCreationCallback<FertilizerViewModel.Factory> { factory ->
                factory.create(sessionManager.akilimoUser, fertilizerFlow)
            }
        }
    )

    override fun onBindingReady(savedInstanceState: Bundle?) {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = handleBackNavigation()
            }
        )
        initHelpers()
        setupToolbar()
        setupRecycler()
        setupRefreshFab()
        observeSyncWorker()
        observeViewModel()
    }

    private fun initHelpers() {
        listManager = FertilizerListManager(
            recyclerView = getRecyclerView(),
            gridSpanCount = gridSpanCount
        )
        selectionHelper = FertilizerSelectionHelper(
            lifecycleScope = safeScope,
            priceRepo = viewModel.priceRepo,
            selectedRepo = viewModel.selectedRepo
        )
    }

    private fun setupToolbar() {
        val helper = ToolbarHelper(
            requireActivity() as androidx.appcompat.app.AppCompatActivity,
            getToolbar()
        )
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
                context = requireContext(),
                workName = WorkConstants.FERTILIZER_WORK_NAME
            )
        }
    }

    private fun observeViewModel() {
        safeScope.launch {
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
            fragmentManager = childFragmentManager,
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

    private fun observeSyncWorker() {
        WorkManager.getInstance(requireContext())
            .getWorkInfosForUniqueWorkLiveData(WorkConstants.FERTILIZER_WORK_NAME)
            .observe(viewLifecycleOwner) { infos ->
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

    private fun handleBackNavigation() {
        safeScope.launch {
            val status = viewModel.getStepStatus()
            val completion = AdviceCompletionDto(taskName = adviseTask, stepStatus = status)
            parentFragmentManager.setFragmentResult(
                UseCaseResults.ADVICE_COMPLETION,
                bundleOf(UseCaseResults.ADVICE_COMPLETION_DTO to completion)
            )
            findNavController().popBackStack()
        }
    }
}
