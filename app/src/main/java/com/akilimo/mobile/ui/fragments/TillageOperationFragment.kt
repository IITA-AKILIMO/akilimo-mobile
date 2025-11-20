package com.akilimo.mobile.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.ValueOptionAdapter
import com.akilimo.mobile.base.BaseStepFragment
import com.akilimo.mobile.databinding.FragmentTillageOperationBinding
import com.akilimo.mobile.databinding.ItemTillageOperationBinding
import com.akilimo.mobile.dto.OperationEntry
import com.akilimo.mobile.dto.OperationMethodOption
import com.akilimo.mobile.dto.OperationTypeOption
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.enums.EnumOperationMethod
import com.akilimo.mobile.enums.EnumOperationType
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.stepstone.stepper.VerificationError
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass.
 * Use the [TillageOperationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TillageOperationFragment : BaseStepFragment<FragmentTillageOperationBinding>() {
    companion object {
        fun newInstance() = TillageOperationFragment()
    }

    private lateinit var userRepository: AkilimoUserRepo

    private var allOperations: List<OperationTypeOption> = emptyList()
    private var allMethods: List<OperationMethodOption> = emptyList()


    private val operationBindings = mutableMapOf<EnumOperationType, ItemTillageOperationBinding>()

    private val selectedEntries = mutableMapOf<EnumOperationType, OperationEntry>()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentTillageOperationBinding.inflate(inflater, container, false)

    /** Called after binding is safely initialized */
    override fun onBindingReady(savedInstanceState: Bundle?) {
        userRepository = AkilimoUserRepo(database.akilimoUserDao())

        allOperations = listOf(
            OperationTypeOption(
                EnumOperationType.PLOUGHING.label(requireContext()),
                EnumOperationType.PLOUGHING
            ),
            OperationTypeOption(
                EnumOperationType.RIDGING.label(requireContext()),
                EnumOperationType.RIDGING
            ),
            OperationTypeOption(
                EnumOperationType.WEEDING.label(requireContext()),
                EnumOperationType.WEEDING
            ),
        )

        allMethods = listOf(
            OperationMethodOption(
                EnumOperationMethod.TRACTOR.label(requireContext()),
                EnumOperationMethod.TRACTOR
            ),
            OperationMethodOption(
                EnumOperationMethod.MANUAL.label(requireContext()),
                EnumOperationMethod.MANUAL
            ),
        )

        val methodAdapter = ValueOptionAdapter(requireContext(), allMethods)
        allOperations.forEach { operation ->
            ItemTillageOperationBinding.inflate(layoutInflater, binding.containerTillageOperations, true)
                .also { row ->
                    operationBindings[operation.valueOption] = row
                    setupOperationRow(row, operation, methodAdapter)
                }
        }



    }

    private fun setupOperationRow(
        rowBinding: ItemTillageOperationBinding,
        operation: OperationTypeOption,
        methodAdapter: ValueOptionAdapter<EnumOperationMethod>
    ) {
        rowBinding.checkboxOperation.text = operation.displayLabel
        rowBinding.dropTillageMethod.setAdapter(methodAdapter)

        rowBinding.checkboxOperation.setOnCheckedChangeListener { _, isChecked ->
            rowBinding.dropTillageMethod.isEnabled = isChecked
            if (!isChecked) {
                selectedEntries.remove(operation.valueOption)
                rowBinding.dropTillageMethod.setText("", false)
            }
        }


        rowBinding.dropTillageMethod.setOnItemClickListener { _, _, position, _ ->
            val method = methodAdapter.getItem(position)
            selectedEntries[operation.valueOption] = OperationEntry(
                operation,
                method as OperationMethodOption
            )
            rowBinding.dropTillageMethod.setText(method.displayLabel, false)
        }
    }

    override fun prefillFromEntity() {
        safeScope.launch {
            val user = userRepository.getUser(sessionManager.akilimoUser) ?: return@launch
            selectedEntries.clear()
            user.tillageOperations.forEach { entry ->
                val operation = OperationTypeOption(
                    entry.operation.displayLabel,
                    entry.operation.valueOption
                )
                val method = OperationMethodOption(
                    entry.method.displayLabel,
                    entry.method.valueOption
                )

                selectedEntries[operation.valueOption] = OperationEntry(operation, method)
                operationBindings[operation.valueOption]?.apply {
                    checkboxOperation.isChecked = true
                    dropTillageMethod.isEnabled = true
                    dropTillageMethod.setText(method.displayLabel, false)
                }
            }
        }
    }

    override fun verifyStep(): VerificationError? {
        val missing = allOperations.filter { !selectedEntries.containsKey(it.valueOption) }
        if (missing.isNotEmpty()) {
            return VerificationError(getString(R.string.lbl_select_tillage_method))
        }

        safeScope.launch {
            val user = userRepository.getUser(sessionManager.akilimoUser) ?: AkilimoUser()
            user.tillageOperations = selectedEntries.values.toList()
            userRepository.saveOrUpdateUser(user, sessionManager.akilimoUser)
        }

        return null
    }
}