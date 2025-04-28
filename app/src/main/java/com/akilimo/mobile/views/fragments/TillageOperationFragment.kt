package com.akilimo.mobile.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import com.akilimo.mobile.databinding.FragmentTillageOperationBinding
import com.akilimo.mobile.entities.CurrentPractice
import com.akilimo.mobile.inherit.BaseStepFragment
import com.akilimo.mobile.interfaces.IDismissOperationsDialogListener
import com.akilimo.mobile.utils.enums.EnumOperationType
import com.akilimo.mobile.views.fragments.dialog.OperationTypeDialogFragment
import com.stepstone.stepper.VerificationError
import io.sentry.Sentry

/**
 * A simple [Fragment] subclass.
 * Use the [TillageOperationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TillageOperationFragment : BaseStepFragment() {
    private var currentPractice: CurrentPractice? = null
    private var _binding: FragmentTillageOperationBinding? = null
    private val binding get() = _binding!!

    private var performPloughing = false
    private var performRidging = false
    private var performHarrowing = false
    private var isDataRefreshing = false


    private var ploughingMethod: String? = null
    private var ridgingMethod: String? = null
    private var harrowingMethod: String? = null
    private var operation: String? = null

    companion object {
        fun newInstance(): TillageOperationFragment {
            return TillageOperationFragment()
        }
    }

    override fun loadFragmentLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTillageOperationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            chkPloughing.setOnCheckedChangeListener { buttonView: CompoundButton, checked: Boolean ->
                operation = if (checked) "Plough" else "NA"
                performPloughing = checked
                if (buttonView.isPressed) {
                    onCheckboxClicked(checked)
                }
                isDataRefreshing = false
            }
            chkRidging.setOnCheckedChangeListener { buttonView: CompoundButton, checked: Boolean ->
                performRidging = checked
                operation = if (checked) "Ridge" else "NA"
                if (buttonView.isPressed) {
                    onCheckboxClicked(checked)
                }
                isDataRefreshing = false
            }
        }
    }

    fun refreshData() {
        try {
            currentPractice = database.currentPracticeDao().findOne()
            if (currentPractice != null) {
                isDataRefreshing = true
                performPloughing = currentPractice!!.performPloughing
                performRidging = currentPractice!!.performRidging
                ploughingMethod = currentPractice!!.ploughingMethod
                ridgingMethod = currentPractice!!.ridgingMethod

                binding.apply {
                    if (performPloughing) {
                        chkPloughing.isChecked = true
                    }
                    if (performRidging) {
                        chkRidging.isChecked = true
                    }
                }
            }
        } catch (ex: Exception) {
            Toast.makeText(requireContext(), ex.message, Toast.LENGTH_SHORT).show()
            Sentry.captureException(ex)
        }
    }

    private fun onCheckboxClicked(checked: Boolean) {
        if (!checked) {
            saveEntities()
            return
        }
        val arguments = Bundle()
        arguments.putString(OperationTypeDialogFragment.OPERATION_TYPE, operation)

        val operationTypeDialogFragment = OperationTypeDialogFragment()
        operationTypeDialogFragment.arguments = arguments


        val fragmentTransaction = parentFragmentManager.beginTransaction()
        val prev = parentFragmentManager.findFragmentByTag(OperationTypeDialogFragment.ARG_ITEM_ID)
        if (prev != null) {
            fragmentTransaction.remove(prev)
        }
        fragmentTransaction.addToBackStack(null)
        operationTypeDialogFragment.show(
            parentFragmentManager,
            OperationTypeDialogFragment.ARG_ITEM_ID
        )


        operationTypeDialogFragment.setOnDismissListener(object : IDismissOperationsDialogListener {
            override fun onDismiss(
                operation: String,
                enumOperationType: EnumOperationType,
                cancelled: Boolean
            ) {
                when (operation.uppercase()) {
                    "PLOUGH" -> {
                        performPloughing = !cancelled
                        binding.chkPloughing.isChecked = !cancelled
                        ploughingMethod = enumOperationType.operationName()
                    }

                    "RIDGE" -> {
                        performRidging = !cancelled
                        binding.chkRidging.isChecked = !cancelled
                        ridgingMethod = enumOperationType.operationName()
                    }

                    else -> {
                        ridgingMethod = EnumOperationType.NONE.operationName()
                        ploughingMethod = EnumOperationType.NONE.operationName()
                        harrowingMethod = EnumOperationType.NONE.operationName()
                        run {
                            performRidging = false
                            performPloughing = false
                            performHarrowing = false
                        }
                    }
                }
                saveEntities()
            }
        })
    }

    private fun saveEntities() {

        try {
            if (currentPractice == null) {
                currentPractice = CurrentPractice()
            }

            currentPractice!!.ridgingMethod = ridgingMethod
            currentPractice!!.ploughingMethod = ploughingMethod
            currentPractice!!.performRidging = performRidging
            currentPractice!!.performPloughing = performPloughing
            currentPractice!!.performHarrowing = performHarrowing

            if (currentPractice!!.id != null) {
                database.currentPracticeDao().update(currentPractice!!)
            } else {
                database.currentPracticeDao().insert(currentPractice!!)
            }

            currentPractice = database.currentPracticeDao().findOne()
            dataIsValid = true
        } catch (ex: Exception) {
            dataIsValid = false
            errorMessage = ex.message!!
            Sentry.captureException(ex)
        }
    }

    override fun verifyStep(): VerificationError? {
        saveEntities()
        if (!dataIsValid) {
            showCustomWarningDialog(errorMessage)
            return VerificationError(errorMessage)
        }
        return null
    }

    override fun onSelected() {
        refreshData()
    }

    override fun onError(error: VerificationError) {
        //Not implemented
    }
}
