package com.akilimo.mobile.views.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.DialogFieldSizeBinding
import com.akilimo.mobile.databinding.FragmentFieldSizeBinding
import com.akilimo.mobile.inherit.BindBaseStepFragment
import com.akilimo.mobile.utils.LanguageManager
import com.akilimo.mobile.utils.enums.EnumAreaUnit
import com.akilimo.mobile.utils.enums.EnumFieldArea
import com.stepstone.stepper.VerificationError
import io.sentry.Sentry

class FieldSizeFragment : BindBaseStepFragment<FragmentFieldSizeBinding>() {

    private var myFieldSize: String? = null
    private var areaSizeInput = 0.0
    private var isExactArea = false
    private var areaUnitChanged = false
    private var areaUnitInput: String = ""
    private var displayAreaUnitInput: String = ""

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
            rdFieldSizeQuarterAcre.setOnClickListener { radioSelected(R.id.rd_field_size_quarter_acre) }
            rdFieldSizeHalfAcre.setOnClickListener { radioSelected(R.id.rd_field_size_half_acre) }
            rdFieldSizeOneAcre.setOnClickListener { radioSelected(R.id.rd_field_size_one_acre) }
            rdFieldSizeTwoHalfAcre.setOnClickListener { radioSelected(R.id.rd_field_size_two_half_acre) }
            rdFieldSizeSpecifyArea.setOnClickListener {
                showCustomDialog()
                radioSelected(R.id.rd_field_size_specify_area)
            }
        }
    }

    override fun onSelected() {
        refreshData()
    }

    override fun verifyStep(): VerificationError? {
        return if (areaSizeInput <= 0.0 || dataIsValid == false) {
            errorMessage = getString(R.string.lbl_field_size_prompt, displayAreaUnitInput)
            VerificationError(errorMessage)
        } else null
    }

    private fun refreshData() {
        try {
            dataIsValid = false
            val mandatoryInfo = database.mandatoryInfoDao().findOne() ?: return

            with(mandatoryInfo) {
                areaUnitChanged = !areaUnit.equals(oldAreaUnit, ignoreCase = true)
                areaUnitInput = areaUnit
                displayAreaUnitInput = displayAreaUnit
                isExactArea = exactArea
                areaSizeInput = areaSize
                myFieldSize = areaSize.toString()

                dataIsValid = areaSizeInput > 0.0
                setFieldLabels(areaUnit)

                val matchedOption = when (areaSizeInput) {
                    EnumFieldArea.QUARTER_ACRE.areaValue() -> binding.rdFieldSizeQuarterAcre.id
                    EnumFieldArea.HALF_ACRE.areaValue() -> binding.rdFieldSizeHalfAcre.id
                    EnumFieldArea.ONE_ACRE.areaValue() -> binding.rdFieldSizeOneAcre.id
                    EnumFieldArea.TWO_HALF_ACRE.areaValue() -> binding.rdFieldSizeTwoHalfAcre.id
                    else -> binding.rdFieldSizeSpecifyArea.id
                }

                binding.rdgFieldSize.check(matchedOption)
                if (matchedOption == binding.rdFieldSizeSpecifyArea.id) {
                    setExactAreaText(areaSizeInput, displayAreaUnit)
                }

                binding.rdFieldSizeSpecifyArea.visibility =
                    if (isExactArea) View.VISIBLE else View.GONE
            }
        } catch (ex: Exception) {
            Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
            Sentry.captureException(ex)
        }
    }

    private fun radioSelected(checkedId: Int) {
        binding.txtFieldSizeSpecifiedArea.visibility = View.GONE
        isExactArea = false

        areaSizeInput = when (checkedId) {
            R.id.rd_field_size_quarter_acre -> EnumFieldArea.QUARTER_ACRE.areaValue()
            R.id.rd_field_size_half_acre -> EnumFieldArea.HALF_ACRE.areaValue()
            R.id.rd_field_size_one_acre -> EnumFieldArea.ONE_ACRE.areaValue()
            R.id.rd_field_size_two_half_acre -> EnumFieldArea.TWO_HALF_ACRE.areaValue()
            R.id.rd_field_size_specify_area -> {
                isExactArea = true
                EnumFieldArea.EXACT_AREA.areaValue()
            }

            else -> return
        }

        if (!isExactArea) {
            val convertedAreaSize =
                mathHelper.convertFromAcreToSpecifiedArea(areaSizeInput, areaUnitInput)
            saveFieldSize(convertedAreaSize)
        }
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
            fieldSizeTitle.text = getString(R.string.lbl_cassava_field_size, displayAreaUnitInput)
        }
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

    private fun showCustomDialog() {
        val dialog = Dialog(requireContext()).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(true)
        }

        val dialogBinding = DialogFieldSizeBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialogBinding.dialogTitle.text =
            getString(R.string.lbl_cassava_field_size, displayAreaUnitInput)

        myFieldSize?.takeIf { isExactArea }?.let {
            dialogBinding.etPost.setText(it)
        }

        dialogBinding.btCancel.setOnClickListener {
            dialog.dismiss()
            binding.rdgFieldSize.clearCheck()
            areaSizeInput = -1.0
            isExactArea = false
        }

        dialogBinding.btSubmit.setOnClickListener {
            val input = dialogBinding.etPost.text.toString().trim()
            if (input.isEmpty()) {
                Toast.makeText(
                    context,
                    getString(R.string.lbl_field_size_prompt, displayAreaUnitInput),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                myFieldSize = input
                areaSizeInput = input.toDouble()
                setExactAreaText(areaSizeInput, displayAreaUnitInput)
                saveFieldSize(areaSizeInput)
                binding.txtFieldSizeSpecifiedArea.visibility = View.VISIBLE
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun saveFieldSize(convertedAreaSize: Double) {
        if (convertedAreaSize <= 0) {
            showCustomWarningDialog(
                getString(R.string.lbl_field_size_prompt, displayAreaUnitInput),
                getString(R.string.lbl_field_size_prompt, displayAreaUnitInput)
            )
            return
        }

        areaUnitChanged = false // Reset flag

        try {
            database.mandatoryInfoDao().findOne()?.let { info ->
                info.apply {
                    areaSize = convertedAreaSize
                    oldAreaUnit = areaUnit
                    exactArea = isExactArea
                }

                dataIsValid = areaSizeInput > 0.0
                database.mandatoryInfoDao().update(info)
            } ?: Toast.makeText(
                context,
                "Mandatory user information not provided",
                Toast.LENGTH_SHORT
            ).show()
        } catch (ex: Exception) {
            Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
        }
    }
}