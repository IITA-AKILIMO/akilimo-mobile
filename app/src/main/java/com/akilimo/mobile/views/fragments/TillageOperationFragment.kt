package com.akilimo.mobile.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.FragmentTillageOperationBinding
import com.akilimo.mobile.entities.CurrentPractice
import com.akilimo.mobile.inherit.BaseStepFragment
import com.akilimo.mobile.interfaces.DefaultDispatcherProvider
import com.akilimo.mobile.interfaces.IDismissOperationsDialogListener
import com.akilimo.mobile.interfaces.IDispatcherProvider
import com.akilimo.mobile.utils.enums.EnumOperation
import com.akilimo.mobile.utils.enums.EnumOperationMethod
import com.akilimo.mobile.utils.showDialogFragmentSafely
import com.akilimo.mobile.views.fragments.dialog.OperationTypeDialogFragment
import com.google.android.material.button.MaterialButton
import com.stepstone.stepper.VerificationError
import io.sentry.Sentry
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TillageOperationFragment(private val dispatchers: IDispatcherProvider = DefaultDispatcherProvider()) :
    BaseStepFragment() {
    private var _binding: FragmentTillageOperationBinding? = null
    private val binding get() = _binding!!
    override var dataIsValid: Boolean = true

    private var currentPractice: CurrentPractice? = null

    companion object {
        private const val TAG = "TillOperationFragment"
        fun newInstance(): TillageOperationFragment {
            return TillageOperationFragment()
        }
    }

    override fun loadFragmentLayout(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTillageOperationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load initial data from database in the background
        loadCurrentPractice()

        binding.apply {
            tillageOperationsGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
                val button = group.findViewById<MaterialButton>(checkedId)
                if (button.isPressed) {
                    handleTillageOperationChange(checkedId, isChecked)
                }
            }
        }
    }

    private fun loadCurrentPractice() {
        viewLifecycleOwner.lifecycleScope.launch {
            val startTime = System.currentTimeMillis()
            Log.d(TAG, "Loading current practice from database...")
            try {
                // Perform database read on a background thread
                currentPractice = withContext(dispatchers.io) {
                    database.currentPracticeDao().findOne() ?: CurrentPractice()
                }
                updateUiFromCurrentPractice()
                val endTime = System.currentTimeMillis()
                Log.d(TAG, "Current practice loaded in ${endTime - startTime} ms")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading current practice", e)
                Sentry.captureException(e)
            }
        }
    }

    private fun updateUiFromCurrentPractice() {
        currentPractice?.let { practice ->
            binding.tillageBtnPloughing.isChecked = practice.performPloughing
            binding.tillageBtnRidging.isChecked = practice.performRidging
        }
    }


    private fun handleTillageOperationChange(checkedId: Int, isChecked: Boolean) {
        val practice = currentPractice ?: CurrentPractice().also { currentPractice = it }

        var practiceChanged = false
        when (checkedId) {
            R.id.tillage_btn_ploughing -> {
                if (isChecked) {
                    showOperationsDialog(EnumOperation.TILLAGE)
                } else {
                    if (practice.performPloughing) { // Check if value actually changed
                        practice.performPloughing = false
                        practice.ploughingMethod =
                            EnumOperationMethod.NONE // Reset method if operation is disabled
                        practiceChanged = true
                    }
                }
            }

            R.id.tillage_btn_ridging -> {
                if (isChecked) {
                    showOperationsDialog(EnumOperation.RIDGING)
                } else {
                    if (practice.performRidging) { // Check if value actually changed
                        practice.performRidging = false
                        practice.ridgingMethod =
                            EnumOperationMethod.NONE // Reset method if operation is disabled
                        practiceChanged = true
                    }
                }
            }
        }

        if (practiceChanged) {
            saveCurrentPracticeToDatabase()
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

                val practice = currentPractice ?: CurrentPractice().also { currentPractice = it }
                var practiceModifiedInDialog = false

                when (operationName) {
                    EnumOperation.TILLAGE -> {
                        if (enumOperationMethod != EnumOperationMethod.NONE) {
                            if (!practice.performPloughing || practice.ploughingMethod != enumOperationMethod) {
                                practice.performPloughing = true
                                practice.ploughingMethod = enumOperationMethod
                                practiceModifiedInDialog = true
                            }
                        } else {
                            if (practice.performPloughing) {
                                practice.performPloughing = false
                                practice.ploughingMethod = EnumOperationMethod.NONE
                                practiceModifiedInDialog = true
                            }
                        }
                    }

                    EnumOperation.RIDGING -> { // Assuming this dialog is for ridging
                        if (enumOperationMethod != EnumOperationMethod.NONE) {
                            if (!practice.performRidging || practice.ridgingMethod != enumOperationMethod) {
                                practice.performRidging = true
                                practice.ridgingMethod = enumOperationMethod
                                practiceModifiedInDialog = true
                            }
                        } else {
                            if (practice.performRidging) {
                                practice.performRidging = false
                                practice.ridgingMethod = EnumOperationMethod.NONE
                                practiceModifiedInDialog = true
                            }
                        }
                    }

                    else -> {
                        Log.w(TAG, "Unhandled operation name in dialog dismiss: $operationName")
                    }
                }

                if (practiceModifiedInDialog) {
                    updateUiFromCurrentPractice()
                    saveCurrentPracticeToDatabase()
                }
            }
        })

        showDialogFragmentSafely(
            parentFragmentManager,
            operationTypeDialogFragment,
            OperationTypeDialogFragment.TAG_OPERATION_DIALOG
        )
    }


    private fun saveCurrentPracticeToDatabase() {
        val practiceToSave = currentPractice ?: return // Nothing to save

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(dispatchers.io) {
                    database.currentPracticeDao().insert(practiceToSave)
                }
                dataIsValid = true
            } catch (ex: Exception) {
                dataIsValid = false
                errorMessage = ex.message ?: "Unknown error during save"
                Log.e(TAG, "Error saving current practice: $errorMessage", ex)
                Sentry.captureException(ex)
            }
        }
    }

    override fun verifyStep(): VerificationError? {
        // Data validation should ideally check the current state of `currentPractice`
        // For example: if (currentPractice?.performPloughing == true && currentPractice?.ploughingMethod == EnumOperationMethod.NONE)
        // For now, it relies on the dataIsValid flag which is set after save attempts.
        if (!dataIsValid) {
            showCustomWarningDialog(errorMessage)
            return VerificationError(errorMessage)
        }
        return null
    }

    override fun onSelected() {
        // Data should already be loaded in onViewCreated or via ViewModel.
        // UI should be up-to-date. If not, trigger a reload or UI update.
        Log.d(TAG, "TillageOperationFragment selected.")
        if (currentPractice == null) {
            loadCurrentPractice() // Load if not already loaded (e.g., if fragment was recreated)
        } else {
            updateUiFromCurrentPractice() // Ensure UI is consistent with cached data
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Important for preventing memory leaks
        Log.d(TAG, "onDestroyView called.")
    }
}