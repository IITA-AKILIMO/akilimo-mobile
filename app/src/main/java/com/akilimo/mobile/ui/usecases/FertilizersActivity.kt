package com.akilimo.mobile.ui.usecases

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.FertilizerAdapter
import com.akilimo.mobile.adapters.GenericSelectableAdapter
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityFertilizersBinding
import com.akilimo.mobile.databinding.DialogPriceSelectionBinding
import com.akilimo.mobile.entities.AdviceCompletionDto
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.entities.FertilizerPrice
import com.akilimo.mobile.entities.SelectedFertilizer
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.FertilizerPriceRepo
import com.akilimo.mobile.repos.FertilizerRepo
import com.akilimo.mobile.repos.SelectedFertilizerRepo
import com.akilimo.mobile.ui.components.ToolbarHelper
import com.akilimo.mobile.workers.FertilizerWorker
import com.akilimo.mobile.workers.WorkConstants
import com.akilimo.mobile.workers.WorkerScheduler
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FertilizersActivity : BaseActivity<ActivityFertilizersBinding>() {

    private lateinit var fertilizerAdapter: FertilizerAdapter
    private lateinit var userRepo: AkilimoUserRepo
    private lateinit var fertilizerRepo: FertilizerRepo
    private lateinit var selectedRepo: SelectedFertilizerRepo
    private lateinit var priceRepo: FertilizerPriceRepo

    private val gridSpanCount by lazy { resources.getInteger(R.integer.grid_span_count_default) }

    override fun inflateBinding() = ActivityFertilizersBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        initRepositories()
        setupToolbar()
        setupRecycler()
        setupFab()
        observeFertilizers()
        updateRecyclerLayout()

        onBackPressedDispatcher
    }

    private fun initRepositories() {
        userRepo = AkilimoUserRepo(database.akilimoUserDao())
        fertilizerRepo = FertilizerRepo(database.fertilizerDao())
        selectedRepo = SelectedFertilizerRepo(database.selectedFertilizerDao())
        priceRepo = FertilizerPriceRepo(database.fertilizerPriceDao())
    }

    private fun setupToolbar() {
        ToolbarHelper(this, binding.lytToolbar.toolbar).showBackButton(true)
            .inflateMenu(R.menu.menu_fertilizers) { item ->
                if (item.itemId == R.id.action_toggle_layout) toggleLayout(item)
            }.onNavigationClick {
                handleBackNavigation()
            }.build()
    }

    override fun handleBackPressed(): Boolean {
        handleBackNavigation()
        return true
    }

    private fun toggleLayout(item: android.view.MenuItem) {
        val isGridLayout = !sessionManager.isFertilizerGrid
        sessionManager.isFertilizerGrid = isGridLayout
        updateRecyclerLayout()
        item.setIcon(if (isGridLayout) R.drawable.ic_list else R.drawable.ic_grid)
    }

    private fun setupRecycler() {
        fertilizerAdapter = FertilizerAdapter().apply {
            onItemClick = { fertilizer -> showPricesForFertilizer(fertilizer) }
        }

        binding.fertilizersList.apply {
            layoutManager = LinearLayoutManager(this@FertilizersActivity)
            adapter = this@FertilizersActivity.fertilizerAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupFab() {
        binding.fabRefresh.setOnClickListener {
            it.isEnabled = false
            WorkerScheduler.scheduleOneTimeWorker<FertilizerWorker>(
                context = this,
                workName = WorkConstants.FERTILIZER_WORK_NAME
            )
        }
    }

    private fun observeFertilizers() = safeScope.launch {
        val user = userRepo.getUser(sessionManager.akilimoUser) ?: return@launch
        val userId = user.id ?: return@launch
        val country = user.farmCountry.orEmpty()

        launch {
            fertilizerRepo.observeByCountry(country).collectLatest { fertilizers ->
                val selectedIds =
                    selectedRepo.getSelectedSync(userId).map { it.fertilizerId }.toSet()
                val selectedList = selectedRepo.getSelectedSync(userId)
                val selectedMap = selectedList.associateBy { it.fertilizerId }

                val mapped = fertilizers.map { fertilizer ->
                    fertilizer.apply {
                        val selected = selectedMap[id]
                        isSelected = selectedIds.contains(id)
                        displayPrice = selected?.displayPrice.orEmpty()
                        selectedPrice = if (isSelected) selected?.fertilizerPrice ?: 0.0 else 0.0
                    }
                }

                updateUI(mapped)
            }
        }

        launch {
            selectedRepo.observeSelected(userId).collectLatest { selectedList ->
                val mapped = selectedList.map { it.fertilizerId }.toSet()
                fertilizerAdapter.updateSelection(mapped)
            }
        }
    }

    private fun updateUI(fertilizers: List<Fertilizer>) {
        if (fertilizers.isEmpty()) return showEmptyState(true)
        fertilizerAdapter.submitList(fertilizers)
        showEmptyState(false)
    }

    private fun updateRecyclerLayout() {
        val isGridLayout = sessionManager.isFertilizerGrid
        binding.fertilizersList.layoutManager =
            if (isGridLayout) GridLayoutManager(this, gridSpanCount) else LinearLayoutManager(this)
        fertilizerAdapter.setLayoutMode(isGridLayout)
    }

    private fun showEmptyState(isEmpty: Boolean) {
        binding.emptyState.isVisible = isEmpty
        binding.fertilizersList.isVisible = !isEmpty
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
                binding.fabRefresh.isEnabled = true
                val saved = workInfo.outputData.getInt("savedCount", 0)
                Snackbar.make(
                    binding.root,
                    getString(R.string.fertilizers_synced, saved),
                    Snackbar.LENGTH_SHORT
                ).show()
            }

            else -> {
                showSyncIndicator(false)
                binding.fabRefresh.isEnabled = true
            }
        }
    }

    private fun showSyncIndicator(visible: Boolean) {
        binding.syncIndicator.isVisible = visible
    }

    private fun showPricesForFertilizer(fertilizer: Fertilizer) = safeScope.launch {
        val prices = priceRepo.getByFertilizerKey(fertilizer.key.orEmpty())
        if (prices.isEmpty()) return@launch

        val selectedId =
            selectedRepo.getSelectedByFertilizer(fertilizer.id ?: 0)?.fertilizerPriceId ?: 0
        val updatedPriceList: List<FertilizerPrice> = prices.map {
            it.copy().apply {
                isSelected = it.id == selectedId
            }
        }
        showPriceSelectionDialog(fertilizer, updatedPriceList, selectedId)
    }

    private fun showPriceSelectionDialog(
        fertilizer: Fertilizer, prices: List<FertilizerPrice>, preselectedPriceId: Int = 0
    ) {
        val binding = DialogPriceSelectionBinding.inflate(layoutInflater)
        var selectedFertilizerPrice: FertilizerPrice? = null

        if (preselectedPriceId > 0) {
            binding.removeSelectionButton.visibility = View.VISIBLE
            safeScope.launch {
                val fertilizerId = fertilizer.id ?: return@launch
                val selectedPrice =
                    selectedRepo.getSelectedByFertilizer(fertilizerId) ?: return@launch
            }
        } else {
            binding.removeSelectionButton.visibility = View.GONE
        }

        val fertilizerPriceAdapter = GenericSelectableAdapter<FertilizerPrice>(
            scope = lifecycleScope,
            getId = { it.id },
            getLabel = {
                val countryEnum = EnumCountry.valueOf(it.countryCode)
                when {
                    it.pricePerBag == 0.0 -> getString(R.string.lbl_do_not_know)
                    it.pricePerBag < 0.0 -> getString(
                        R.string.exact_fertilizer_price_currency,
                        countryEnum.currencyName()
                    )

                    else -> it.priceRange
                }
            },
            isSelected = { it.isSelected },
            isExactPrice = { it.pricePerBag < 0.0 },
            onItemClick = { selectedPrice ->
                selectedFertilizerPrice = selectedPrice
            },
            onExactAmount = { selectedPrice, amount ->
                selectedFertilizerPrice =
                    selectedPrice.copy(pricePerBag = amount)
            }
        )
        binding.priceRecycler.apply {
            layoutManager = LinearLayoutManager(this@FertilizersActivity)
            adapter = fertilizerPriceAdapter
        }
        fertilizerPriceAdapter.updateItems(prices)

        val titleText = getString(R.string.price_per_bag, fertilizer.name, fertilizer.countryCode)


        val dialog = AlertDialog.Builder(this).setTitle(titleText)
            .setView(binding.root).setPositiveButton(getString(R.string.lbl_save)) { _, _ ->
                saveSelectedPrice(fertilizer, selectedFertilizerPrice)
            }.setNegativeButton(getString(R.string.lbl_cancel), null).create()

        binding.removeSelectionButton.setOnClickListener {
            removeSelectedFertilizer(fertilizer, dialog)
        }

        dialog.show()
    }

    private fun saveSelectedPrice(
        fertilizer: Fertilizer,
        selectedPrice: FertilizerPrice?
    ) = safeScope.launch {
        val user = userRepo.getUser(sessionManager.akilimoUser) ?: return@launch
        val userId = user.id ?: 0
        val fertilizerId = fertilizer.id ?: 0

        val bagPrice = selectedPrice?.pricePerBag
        val finalPrice = when {
            bagPrice != null && bagPrice >= 0.0 -> bagPrice
            else -> {
                selectedPrice?.pricePerBag
            }
        }
        var displayPrice = selectedPrice?.priceRange
        var isExactPrice = false
        if (bagPrice != null && bagPrice < 0.0) {
            displayPrice = "$finalPrice ${selectedPrice.currencySymbol}"
            isExactPrice = true
        }

        if (finalPrice != null && finalPrice >= 0.0) {
            val selectedFertilizer = SelectedFertilizer(
                userId = userId,
                fertilizerId = fertilizerId,
                fertilizerPriceId = selectedPrice?.id ?: 0,
                fertilizerPrice = finalPrice,
                displayPrice = displayPrice,
                isExactPrice = isExactPrice
            )
            selectedRepo.select(selectedFertilizer)
            // update adapter immediately
            fertilizerAdapter.currentList.find { it.id == fertilizerId }?.apply {
                this.selectedPrice = finalPrice
                this.displayPrice = displayPrice
                this.isSelected = true
            }
            fertilizerAdapter.notifyItemRangeChanged(0, fertilizerAdapter.itemCount)
        }
    }

    private fun removeSelectedFertilizer(
        fertilizer: Fertilizer, dialog: androidx.appcompat.app.AlertDialog
    ) = safeScope.launch {
        val user = userRepo.getUser(sessionManager.akilimoUser) ?: return@launch
        val userId = user.id ?: return@launch
        val fertilizerId = fertilizer.id ?: return@launch

        selectedRepo.deselect(userId, fertilizerId)
        fertilizerAdapter.currentList.find { it.id == fertilizerId }?.apply {
            isSelected = false
            selectedPrice = 0.0
        }
        fertilizerAdapter.notifyItemRangeChanged(0, fertilizerAdapter.itemCount)
        dialog.dismiss()
    }

    private fun handleBackNavigation() = safeScope.launch {
        val user = userRepo.getUser(sessionManager.akilimoUser) ?: return@launch
        val userId = user.id ?: 0

        val selected = selectedRepo.getSelectedSync(userId)
        val isCompleted = selected.isNotEmpty()

        val completion = AdviceCompletionDto(
            taskName = EnumAdviceTask.AVAILABLE_FERTILIZERS,
            stepStatus = if (isCompleted) EnumStepStatus.COMPLETED else EnumStepStatus.IN_PROGRESS

        )

        val data = Intent().putExtra(COMPLETED_TASK, completion)
        setResult(RESULT_OK, data)
        finish()
    }


}
