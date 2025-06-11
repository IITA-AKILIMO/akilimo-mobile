package com.akilimo.mobile.views.activities

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.FertilizerGridAdapter
import com.akilimo.mobile.databinding.ActivityFertilizersBinding
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.inherit.BindBaseActivity
import com.akilimo.mobile.interfaces.IFertilizerDismissListener
import com.akilimo.mobile.utils.Tools.dpToPx
import com.akilimo.mobile.utils.enums.EnumAdviceTask
import com.akilimo.mobile.utils.showDialogFragmentSafely
import com.akilimo.mobile.viewmodels.FertilizersViewModel
import com.akilimo.mobile.viewmodels.FertilizersViewModelFactory
import com.akilimo.mobile.views.fragments.dialog.FertilizerPriceDialogFragment
import com.akilimo.mobile.widget.SpacingItemDecoration
import com.google.android.material.snackbar.Snackbar

abstract class BaseFertilizersActivity(
    private val minSelection: Int
) : BindBaseActivity<ActivityFertilizersBinding>() {

    protected lateinit var viewModel: FertilizersViewModel
    protected abstract val mAdapter: FertilizerGridAdapter

    companion object {
        var useCaseTag: String = "useCase"
    }

    override fun inflateBinding() = ActivityFertilizersBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val useCase = intent?.getStringExtra(useCaseTag)

        val factory = FertilizersViewModelFactory(
            application = this.application,
            minSelection = minSelection,
            useCase = useCase
        )
        viewModel = ViewModelProvider(this, factory)[FertilizersViewModel::class.java]

        setupUI()
        observeViewModel()
        viewModel.loadFertilizers()
    }

    private fun setupUI() {
        setupToolbar(binding.toolbarLayout.toolbar, R.string.title_activity_fertilizer_choice) {
            validate(false)
        }
        
        binding.availableFertilizers.apply {
            layoutManager = GridLayoutManager(context, 2)
            addItemDecoration(SpacingItemDecoration(2, dpToPx(context, 3), true))
            setHasFixedSize(true)
            adapter = mAdapter
            visibility = View.GONE
        }

        mAdapter.setOnItemClickListener(object : FertilizerGridAdapter.OnItemClickListener {
            override fun onItemClick(view: View, fertilizer: Fertilizer, position: Int) {
                mAdapter.setActiveRowIndex(position)
                openPriceDialog(fertilizer)
            }
        })

        binding.twoButtons.btnFinish.text = getString(R.string.lbl_finish)

        binding.twoButtons.btnFinish.setOnClickListener {
            val isMinSelected = viewModel.isMinSelected()
            val adviceStatus = AdviceStatus(
                EnumAdviceTask.AVAILABLE_FERTILIZERS.name, isMinSelected
            )
            if (isMinSelected) {
                database.adviceStatusDao().insert(adviceStatus)
                closeActivity(false)
            }
        }

        binding.twoButtons.btnCancel.setOnClickListener {
            closeActivity(false)
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.availableFertilizers.visibility = View.GONE
            viewModel.refreshFertilizers()
        }

        binding.btnRetry.setOnClickListener {
            viewModel.loadFertilizers()
        }
    }

    private fun observeViewModel() {
        viewModel.loading.observe(this) { isLoading ->
            binding.lytProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (!isLoading) {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

        viewModel.error.observe(this) { hasError ->
            val visibility = if (hasError) View.VISIBLE else View.GONE
            binding.errorLabel.visibility = visibility
            binding.errorImage.visibility = visibility
            binding.btnRetry.visibility = visibility
            if (hasError) {
                binding.availableFertilizers.visibility = View.GONE
            }
        }

        viewModel.fertilizers.observe(this) { fertilizers ->
            if (fertilizers.isNotEmpty()) {
                mAdapter.setFertilizers(fertilizers)
                binding.availableFertilizers.visibility = View.VISIBLE
            } else {
                binding.availableFertilizers.visibility = View.GONE
            }
        }

        viewModel.fertilizerUpdated.observe(this) { fertilizer ->
            val position = mAdapter.getAll().indexOfFirst {
                it.fertilizerKey == fertilizer.fertilizerKey
            }
            if (position != -1) {
                mAdapter.setFertilizer(fertilizer, position)
                mAdapter.setActiveRowIndex(position)
                mAdapter.notifyItemChanged(position)
            }
        }

        viewModel.showSnackBarEvent.observe(this) { message ->
            message?.let {
                Snackbar.make(binding.lytProgress, it, Snackbar.LENGTH_SHORT).show()
                viewModel.clearSnackBarEvent()
            }
        }
    }

    private fun openPriceDialog(fertilizer: Fertilizer) {
        fertilizer.countryCode = viewModel.countryCode

        val args = Bundle().apply {
            putParcelable(FertilizerPriceDialogFragment.FERTILIZER_TYPE, fertilizer)
        }

        val dialog = FertilizerPriceDialogFragment().apply {
            arguments = args
            setOnDismissListener(object : IFertilizerDismissListener {
                override fun onDismiss(
                    priceSpecified: Boolean,
                    fertilizer: Fertilizer,
                    removeSelected: Boolean
                ) {
                    val shouldUpdate = priceSpecified || removeSelected
                    if (!shouldUpdate) return

                    fertilizer.selected = !removeSelected
                    viewModel.saveSelectedFertilizer(fertilizer)
                }
            })
        }

        showDialogFragmentSafely(
            supportFragmentManager,
            dialog,
            FertilizerPriceDialogFragment.ARG_ITEM_ID
        )
    }

    override fun validate(backPressed: Boolean) {
        if (viewModel.isMinSelected()) {
            closeActivity(backPressed)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        validate(true)
    }
}
