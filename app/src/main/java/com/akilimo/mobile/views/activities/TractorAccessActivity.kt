package com.akilimo.mobile.views.activities

import android.os.Bundle
import androidx.activity.viewModels
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ActivityTractorAccessBinding
import com.akilimo.mobile.entities.OperationCost
import com.akilimo.mobile.inherit.CostBaseActivity
import com.akilimo.mobile.utils.enums.EnumOperation
import com.akilimo.mobile.utils.enums.EnumOperationMethod
import com.akilimo.mobile.utils.ui.SnackBarMessage
import com.akilimo.mobile.viewmodels.TractorOperationCostViewModel
import com.akilimo.mobile.viewmodels.factory.OperationCostViewModelFactory
import com.akilimo.mobile.views.fragments.dialog.OperationCostsDialogFragment
import com.google.android.material.snackbar.Snackbar

class TractorAccessActivity : CostBaseActivity<ActivityTractorAccessBinding>() {

    private val viewModel: TractorOperationCostViewModel by viewModels {
        OperationCostViewModelFactory(application = this.application, mathHelper = mathHelper)
    }

    override fun inflateBinding() = ActivityTractorAccessBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar(binding.toolbar, R.string.title_tillage_operations) {
            viewModel.validateAndSave(false)
        }
        binding.twoButtons.btnFinish.setOnClickListener { viewModel.validateAndSave(false) }
        binding.twoButtons.btnCancel.setOnClickListener { closeActivity(false) }

        setupListeners()
        setupObservers()
    }


    private fun setupListeners() = with(binding.tractorAccess) {
        tractorToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                viewModel.onTractorSelected(checkedId == R.id.btn_yes_tractor)
            }
        }

        implementToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_plough -> viewModel.onImplementSelected(EnumOperation.TILLAGE)
                    R.id.btn_ridger -> viewModel.onImplementSelected(EnumOperation.RIDGING)
                }
            }
        }
    }

    override fun setupObservers() {
        viewModel.showOperationDialog.observe(this) { params ->
            val args = Bundle().apply {
                putString(OperationCostsDialogFragment.OPERATION_NAME, params.operation.name)
                putString(OperationCostsDialogFragment.OPERATION_TYPE, params.operationType.name)
                putString(OperationCostsDialogFragment.CURRENCY_CODE, params.currencySymbol)
                putString(OperationCostsDialogFragment.COUNTRY_CODE, params.countryCode)
                putString(OperationCostsDialogFragment.DIALOG_TITLE, params.dialogTitle)
                putString(OperationCostsDialogFragment.EXACT_PRICE_HINT, params.hintText)
            }

            OperationCostsDialogFragment().apply {
                this.arguments = arguments
                setOnDismissListener(object : OperationCostsDialogFragment.IDismissDialog {
                    override fun onDismiss(
                        operationCost: OperationCost?,
                        operationName: String?,
                        selectedCost: Double,
                        cancelled: Boolean,
                        isExactCost: Boolean
                    ) {
                        if (!cancelled && operationName != null) {
                            viewModel.setOperationCost(
                                EnumOperation.valueOf(operationName),
                                selectedCost,
                                isExactCost
                            )
                        }
                    }
                })
            }

            viewModel.showSnackBarEvent.observe(this) { message ->
                message?.let {
                    val message = when (it) {
                        is SnackBarMessage.Text -> it.message
                        is SnackBarMessage.Resource -> getString(it.resId)
                    }
                    Snackbar.make(
                        binding.appBar,
                        message,
                        Snackbar.LENGTH_SHORT
                    ).show()
                    viewModel.clearSnackBarEvent()
                }
            }

            viewModel.closeActivity.observe(this) { backPressed ->
                closeActivity(backPressed)
            }
        }
    }

    override fun showDialogFullscreen(
        operationName: EnumOperation,
        operationType: EnumOperationMethod,
        countryCode: String?,
        dialogTitle: String?,
        hintText: String
    ) {
        TODO("Not yet implemented")
    }
}

