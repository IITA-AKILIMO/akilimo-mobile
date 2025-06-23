package com.akilimo.mobile.views.fragments

// import com.akilimo.mobile.entities.CurrentPractice // No longer directly needed
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.FragmentTillageOperationBinding
import com.akilimo.mobile.inherit.BindBaseStepFragment
import com.akilimo.mobile.interfaces.IDismissOperationsDialogListener
import com.akilimo.mobile.utils.enums.EnumOperation
import com.akilimo.mobile.utils.enums.EnumOperationMethod
import com.akilimo.mobile.utils.showDialogFragmentSafely
import com.akilimo.mobile.viewmodels.TillageOperationViewModel
import com.akilimo.mobile.viewmodels.factory.TillageOperationViewModelFactory
import com.akilimo.mobile.views.fragments.dialog.OperationTypeDialogFragment
import com.google.android.material.button.MaterialButton
import com.stepstone.stepper.VerificationError

class TillageOperationFragment : BindBaseStepFragment<FragmentTillageOperationBinding>() {

    companion object {
        private const val TAG = "TillOperationFragment"
        fun newInstance(): TillageOperationFragment {
            return TillageOperationFragment()
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentTillageOperationBinding =
        FragmentTillageOperationBinding.inflate(inflater, container, false)

    private val viewModel: TillageOperationViewModel by viewModels {
        TillageOperationViewModelFactory(
            application = requireActivity().application,
        )
    }

    override fun onBindingReady(savedInstanceState: Bundle?) {

        viewModel.currentPractice.observe(viewLifecycleOwner) { practice ->
            practice?.let {
                binding.tillageBtnPloughing.apply {
                    isChecked = it.performPloughing
                    text = getString(R.string.label_ploughing_method, it.ploughingMethod.name)
                }
                binding.tillageBtnRidging.apply {
                    isChecked = it.performRidging
                    text = getString(R.string.label_ridging_method, it.ridgingMethod.name)
                }
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                showCustomWarningDialog(it)
                viewModel.errorShown()
            }
        }


        binding.apply {
            tillageOperationsGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
                val button = group.findViewById<MaterialButton>(checkedId)
                if (button.isPressed) { // Only act on user presses
                    handleTillageOperationChange(checkedId, isChecked)
                }
            }
        }
    }

    private fun handleTillageOperationChange(checkedId: Int, isChecked: Boolean) {
        when (checkedId) {
            R.id.tillage_btn_ploughing -> {
                if (isChecked) {
                    showOperationsDialog(EnumOperation.TILLAGE)
                } else {
                    viewModel.updatePloughing(perform = false)
                }
            }

            R.id.tillage_btn_ridging -> {
                if (isChecked) {
                    showOperationsDialog(EnumOperation.RIDGING)
                } else {
                    viewModel.updateRidging(perform = false)
                }
            }
        }
    }

    private fun showOperationsDialog(operationName: EnumOperation) {
        val arguments = Bundle()
        arguments.putParcelable(OperationTypeDialogFragment.OPERATION_TYPE, operationName)
        val operationTypeDialogFragment = OperationTypeDialogFragment()
        operationTypeDialogFragment.arguments = arguments

        operationTypeDialogFragment.setOnDismissListener(object : IDismissOperationsDialogListener {
            override fun onDismiss(enumOperationMethod: EnumOperationMethod, cancelled: Boolean) {
                if (cancelled) {
                    return
                }

                when (operationName) {
                    EnumOperation.TILLAGE -> {
                        val performOperation = enumOperationMethod != EnumOperationMethod.NONE
                        viewModel.updatePloughing(performOperation, enumOperationMethod)
                    }

                    EnumOperation.RIDGING -> {
                        val performOperation = enumOperationMethod != EnumOperationMethod.NONE
                        viewModel.updateRidging(performOperation, enumOperationMethod)
                    }

                    else -> {
                        Log.w(TAG, "Unhandled operation name in dialog dismiss: $operationName")
                    }
                }
            }
        })

        showDialogFragmentSafely(
            parentFragmentManager,
            operationTypeDialogFragment,
            OperationTypeDialogFragment.TAG_OPERATION_DIALOG
        )
    }


    override fun onSelected() {
        viewModel.loadCurrentPractice()
    }

    override fun verifyStep(): VerificationError? {
        val isValid = viewModel.dataIsValid.value != false
        if (!isValid) {
            val error = viewModel.errorMessage.value ?: "Data is invalid."
            return VerificationError(error)
        }
        return null
    }

}