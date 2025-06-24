package com.akilimo.mobile.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.FragmentFieldSizeBinding
import com.akilimo.mobile.inherit.BindBaseStepFragment
import com.akilimo.mobile.utils.LanguageManager
import com.akilimo.mobile.utils.enums.EnumAreaUnit
import com.akilimo.mobile.viewmodels.FieldSizeViewModel
import com.akilimo.mobile.viewmodels.factory.FieldSizeViewModelFactory
import com.akilimo.mobile.views.fragments.dialog.FarmNameDialogFragment
import com.stepstone.stepper.VerificationError

class FieldSizeFragment : BindBaseStepFragment<FragmentFieldSizeBinding>() {

    private val viewModel: FieldSizeViewModel by viewModels {
        FieldSizeViewModelFactory(requireActivity().application, mathHelper)
    }

    companion object {
        fun newInstance() = FieldSizeFragment()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentFieldSizeBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        with(binding) {
            rdFieldSizeQuarterAcre.setOnClickListener { viewModel.onRadioSelected(it.id) }
            rdFieldSizeHalfAcre.setOnClickListener { viewModel.onRadioSelected(it.id) }
            rdFieldSizeOneAcre.setOnClickListener { viewModel.onRadioSelected(it.id) }
            rdFieldSizeTwoHalfAcre.setOnClickListener { viewModel.onRadioSelected(it.id) }
            rdFieldSizeSpecifyArea.setOnClickListener {
                val modal = FarmNameDialogFragment { farmName ->
                    viewModel.saveExactArea(farmName)
                    viewModel.onRadioSelected(it.id)
                }
                modal.show(childFragmentManager, "FieldSizeModal")
            }
        }
        setupObservers()
        viewModel.loadInitialData()
    }

    override fun setupObservers() {
        viewModel.radioCheckId.observe(viewLifecycleOwner) { checkedId ->
            checkedId?.let { binding.rdgFieldSize.check(it) }
        }

        viewModel.displayAreaUnitInput.observe(viewLifecycleOwner) { unitLabel ->
            binding.fieldSizeTitle.text = getString(R.string.lbl_cassava_field_size, unitLabel)
        }

        viewModel.areaUnitInput.observe(viewLifecycleOwner) { areaUnit ->
            setFieldLabels(areaUnit)
        }

        viewModel.areaSizeInput.observe(viewLifecycleOwner) { fieldSize ->
            if (viewModel.isExactArea.value == true) {
                binding.txtFieldSizeSpecifiedArea.visibility = View.VISIBLE
                setExactAreaText(fieldSize, viewModel.displayAreaUnitInput.value.orEmpty())
            } else {
                binding.txtFieldSizeSpecifiedArea.visibility = View.GONE
            }
        }

        viewModel.isExactArea.observe(viewLifecycleOwner) { isExact ->
            binding.rdFieldSizeSpecifyArea.visibility =
                if (isExact) View.VISIBLE else View.GONE
        }
    }

    override fun onSelected() {
        viewModel.loadInitialData()
    }

    override fun verifyStep(): VerificationError? {
        val areaValue = viewModel.areaSizeInput.value ?: 0.0
        val isValid = areaValue > 0.0

        return if (!isValid) {
            val displayUnit = viewModel.displayAreaUnitInput.value.orEmpty()
            errorMessage = getString(R.string.lbl_field_size_prompt, displayUnit)
            VerificationError(errorMessage)
        } else null
    }


    private fun setExactAreaText(areaSize: Double, displayUnit: String) {
        val displayLanguage = LanguageManager.getLanguage(requireContext())
        val fieldSize = mathHelper.removeLeadingZero(areaSize)
        val areaUnitLabel = if (displayLanguage.equals("sw", true)) {
            "$displayUnit $fieldSize"
        } else {
            "$fieldSize $displayUnit"
        }

        binding.txtFieldSizeSpecifiedArea.text = areaUnitLabel
    }

    private fun setFieldLabels(areaUnit: String) {
        val unitEnum = EnumAreaUnit.valueOf(areaUnit)
        val (quarterAcre, halfAcre, oneAcre, twoHalfAcre) = unitEnum.labelResIds()

        binding.apply {
            rdFieldSizeQuarterAcre.setText(quarterAcre)
            rdFieldSizeHalfAcre.setText(halfAcre)
            rdFieldSizeOneAcre.setText(oneAcre)
            rdFieldSizeTwoHalfAcre.setText(twoHalfAcre)
            rdFieldSizeSpecifyArea.setText(R.string.exact_field_area)
        }
    }
}