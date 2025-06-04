package com.akilimo.mobile.views.activities

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.FertilizerGridAdapter
import com.akilimo.mobile.databinding.ActivityFertilizersBinding
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.inherit.BindBaseActivity
import com.akilimo.mobile.interfaces.IFertilizerDismissListener
import com.akilimo.mobile.utils.FertilizerList.removeFertilizerByType
import com.akilimo.mobile.utils.Tools.dpToPx
import com.akilimo.mobile.utils.showDialogFragmentSafely
import com.akilimo.mobile.viewmodels.FertilizersViewModel
import com.akilimo.mobile.views.fragments.dialog.FertilizerPriceDialogFragment
import com.akilimo.mobile.widget.SpacingItemDecoration
import com.google.android.material.snackbar.Snackbar

class FertilizersActivity : BindBaseActivity<ActivityFertilizersBinding>() {

    private val viewModel: FertilizersViewModel by viewModels()

    private lateinit var mAdapter: FertilizerGridAdapter
    private var selectedFertilizers: MutableList<Fertilizer> = mutableListOf()

    companion object {
        var useCaseTag: String = "useCase"
    }

    override fun inflateBinding() = ActivityFertilizersBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intentUseCase = intent?.getStringExtra(useCaseTag)
        viewModel.useCase = intentUseCase ?: "NA"

        setupUI()
        observeViewModel()
        viewModel.loadFertilizers()
    }

    private fun setupUI() {
        setupToolbar(binding.toolbarLayout.toolbar, R.string.title_activity_fertilizer_choice) {
            validateInput(false)
        }

        mAdapter = FertilizerGridAdapter(this)
        binding.availableFertilizers.apply {
            layoutManager = GridLayoutManager(this@FertilizersActivity, 2)
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
            viewModel.saveSelectedFertilizers(selectedFertilizers)
            if (viewModel.isMinSelected()) {
                closeActivity(false)
            }
        }

        binding.twoButtons.btnCancel.setOnClickListener {
            closeActivity(false)
        }

        binding.btnRetry.setOnClickListener {
            viewModel.loadFertilizers()
        }
    }

    private fun observeViewModel() {
        viewModel.loading.observe(this) { isLoading ->
            binding.lytProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
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
                mAdapter.setItems(fertilizers)
                binding.availableFertilizers.visibility = View.VISIBLE
            } else {
                binding.availableFertilizers.visibility = View.GONE
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
        val cleanedFertilizers = selectedFertilizers.toMutableList()
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

                    // Update DB via ViewModel or DAO directly if needed
                    if (removeSelected) {
                        selectedFertilizers = removeFertilizerByType(
                            cleanedFertilizers,
                            fertilizer.fertilizerType!!
                        )
                    } else {
                        selectedFertilizers.add(fertilizer)
                    }
                }
            })
        }
        showDialogFragmentSafely(
            supportFragmentManager,
            dialog,
            FertilizerPriceDialogFragment.ARG_ITEM_ID
        )
    }

    private fun setupToolbar(
        toolbar: androidx.appcompat.widget.Toolbar,
        titleResId: Int,
        onBackAction: () -> Unit
    ) {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(titleResId)
        }
        toolbar.setNavigationOnClickListener {
            onBackAction()
        }
    }

    private fun validateInput(backPressed: Boolean) {
        if (viewModel.isMinSelected()) {
            closeActivity(backPressed)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        validateInput(true)
    }
}