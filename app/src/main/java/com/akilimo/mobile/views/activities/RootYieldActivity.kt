package com.akilimo.mobile.views.activities

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.FieldYieldAdapter
import com.akilimo.mobile.databinding.ActivityRootYieldBinding
import com.akilimo.mobile.entities.UseCase
import com.akilimo.mobile.inherit.BindBaseActivity
import com.akilimo.mobile.utils.Tools.dpToPx
import com.akilimo.mobile.utils.enums.EnumUseCase
import com.akilimo.mobile.utils.showDialogFragmentSafely
import com.akilimo.mobile.viewmodels.RootYieldViewModel
import com.akilimo.mobile.viewmodels.factory.RootYieldViewModelFactory
import com.akilimo.mobile.views.fragments.dialog.RootYieldDialogFragment
import com.akilimo.mobile.widget.SpacingItemDecoration
import java.util.Locale

class RootYieldActivity : BindBaseActivity<ActivityRootYieldBinding>() {

    private val viewModel: RootYieldViewModel by viewModels {
        RootYieldViewModelFactory(application = this.application)
    }
    private lateinit var mAdapter: FieldYieldAdapter


    override fun inflateBinding() = ActivityRootYieldBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar(binding.toolbarLayout.toolbar, R.string.title_activity_cassava_root_yield) {
            validate(false)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                validate(true)
            }
        })

        mAdapter = FieldYieldAdapter(
            showImage = false,
            isItemSelected = { it.yieldAmount == viewModel.yieldData.value?.selectedYieldAmount },
        ) { _, fieldYield, position ->
            val dialogFragment = RootYieldDialogFragment.newInstance(
                selectedFieldYield = fieldYield,
                onConfirmClick = {
                    viewModel.saveYieldSelection(fieldYield)
                    mAdapter.notifyDataSetChanged()
                }
            ).also { dialogFragment ->
                showDialogFragmentSafely(
                    supportFragmentManager,
                    dialogFragment,
                    "RootYieldDialogFragment"
                )
            }
        }

        binding.rootYieldRecycler.apply {
            layoutManager = GridLayoutManager(this@RootYieldActivity, 1)
            addItemDecoration(SpacingItemDecoration(1, dpToPx(this@RootYieldActivity, 3), true))
            setHasFixedSize(true)
            adapter = mAdapter
        }

        binding.twoButtons.apply {
            btnFinish.text = getString(R.string.lbl_finish)
            btnFinish.setOnClickListener { validate(true) }
            btnCancel.setOnClickListener { closeActivity(false) }
        }

        setupObservers()
    }

    override fun setupObservers() {
        viewModel.yieldData.observe(this) { holder ->
            setupTitle(holder.useCase, holder.areaUnit)
        }
        viewModel.yieldOptions.observe(this) { options ->
            mAdapter.submitList(options)
        }
    }

    private fun setupTitle(useCase: UseCase?, areaUnit: String) {
        val unitLabel = when (areaUnit.lowercase(Locale.getDefault())) {
            "ha", "hekta" -> getString(R.string.lbl_ha_yield)
            "are" -> getString(R.string.lbl_are_yield)
            else -> getString(R.string.lbl_acre_yield)
        }

        val titleRes = when (useCase?.useCase) {
            EnumUseCase.FR -> R.string.lbl_typical_yield_question_fr
            else -> R.string.lbl_typical_yield_question
        }

        binding.rootYieldTitle.text = getString(titleRes, unitLabel)
    }

    override fun validate(backPressed: Boolean) {
        if ((viewModel.yieldData.value?.selectedYieldAmount ?: 0.0) <= 0) {
            showCustomWarningDialog(
                getString(R.string.lbl_invalid_yield),
                getString(R.string.lbl_current_field_yield_prompt),
                getString(R.string.lbl_ok)
            )
            return
        }

        closeActivity(false)
    }
}