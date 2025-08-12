package com.akilimo.mobile.inherit

import androidx.activity.viewModels
import androidx.viewbinding.ViewBinding
import com.akilimo.mobile.utils.MathHelper
import com.akilimo.mobile.utils.enums.EnumOperation
import com.akilimo.mobile.utils.enums.EnumOperationMethod
import com.akilimo.mobile.viewmodels.OperationCostViewModel
import com.akilimo.mobile.viewmodels.factory.OperationCostViewModelFactory


abstract class CostBaseActivity<T : ViewBinding> : BindBaseActivity<T>() {

    private val viewModel: OperationCostViewModel by viewModels {
        OperationCostViewModelFactory(this.application, MathHelper())
    }


    protected fun loadOperationCost(
        operationName: EnumOperation,
        operationType: EnumOperationMethod,
        dialogTitle: String,
        hintText: String
    ) {
        viewModel.operationCosts.observe(this) { costList ->
            if (costList.isNotEmpty()) {
                showDialogFullscreen(
                    operationName, operationType, countryCode, dialogTitle, hintText
                )
            }
        }

        viewModel.loadOperationCosts(
            countryCode = countryCode,
            operationName = operationName,
            operationType = operationType
        )
    }

    protected abstract fun showDialogFullscreen(
        operationName: EnumOperation,
        operationType: EnumOperationMethod,
        countryCode: String?,
        dialogTitle: String?,
        hintText: String
    )
}
