package com.akilimo.mobile.views.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.FragmentFieldSizeBinding
import com.akilimo.mobile.inherit.BaseStepFragment
import com.akilimo.mobile.utils.LanguageManager
import com.akilimo.mobile.utils.enums.EnumFieldArea
import com.stepstone.stepper.VerificationError
import io.sentry.Sentry


/**
 * A simple [BaseStepFragment] subclass.
 * Use the [FieldSizeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FieldSizeFragment : BaseStepFragment() {

    private var _binding: FragmentFieldSizeBinding? = null
    private val binding get() = _binding!!

    private var myFieldSize: String? = ""
    private var areaSize = 0.0
    private var isExactArea = false
    private var areaUnitChanged = false
    private var dataValid = false
    private var areaUnit: String = ""
    private var titleMessage: String? = null
    private var displayAreaUnit: String = ""


    companion object {
        fun newInstance(): FieldSizeFragment {
            return FieldSizeFragment()
        }
    }

    override fun loadFragmentLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFieldSizeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.rdgFieldSize.setOnCheckedChangeListener { _, checkedId ->
            val radioButton = binding.root.findViewById<RadioButton>(checkedId)
                ?: return@setOnCheckedChangeListener
            if (radioButton.isPressed) {
                // Only react to user presses, not programmatic selection changes
                radioSelected(checkedId)
            }
        }


        binding.rdFieldSizeSpecifyArea.setOnClickListener { radioButton: View? ->
            if (radioButton != null && radioButton.isPressed) {
                showCustomDialog()
            }
        }
    }

    private fun refreshData() {
        try {
            val mandatoryInfo = database.mandatoryInfoDao().findOne()
            if (mandatoryInfo != null) {
                val oldAreaUnit = mandatoryInfo.oldAreaUnit
                val fieldSizeRadioIndex = mandatoryInfo.fieldSizeRadioIndex

                displayAreaUnit = mandatoryInfo.displayAreaUnit
                areaUnit = mandatoryInfo.areaUnit
                isExactArea = mandatoryInfo.exactArea
                areaSize = mandatoryInfo.areaSize
                myFieldSize = areaSize.toString()
                areaUnitChanged = !areaUnit.equals(oldAreaUnit, ignoreCase = true)




                setFieldLabels(areaUnit)
                if (areaUnitChanged) {
                    areaSize = 0.0
                    myFieldSize = null
                    binding.rdgFieldSize.clearCheck()
                } else {
                    binding.rdgFieldSize.check(fieldSizeRadioIndex)
                }
                if (isExactArea) {
                    setExactAreaText(areaSize, displayAreaUnit)
                    binding.rdFieldSizeSpecifyArea.visibility = View.VISIBLE
                } else {
                    binding.rdFieldSizeSpecifyArea.visibility = View.GONE
                }
            }
        } catch (ex: Exception) {
            Sentry.captureException(ex)
        }
    }

    private fun setExactAreaText(areaSize: Double, displayUnit: String) {
        val displayLanguage = LanguageManager.getLanguage(requireContext())
        val fieldSize = mathHelper.removeLeadingZero(areaSize)
        var areaUnitLabel = String.format("%s %s", fieldSize, displayUnit)
        if (displayLanguage.equals("sw", ignoreCase = true)) {
            areaUnitLabel = String.format("%s %s", displayUnit, fieldSize)
        }
        binding.txtFieldSizeSpecifiedArea.text = areaUnitLabel
    }


    private fun radioSelected(checked: Int) {
        binding.txtFieldSizeSpecifiedArea.visibility = View.GONE
        isExactArea = false
        when (checked) {
            R.id.rd_field_size_quarter_acre -> areaSize = EnumFieldArea.QUARTER_ACRE.areaValue()
            R.id.rd_field_size_half_acre -> areaSize = EnumFieldArea.HALF_ACRE.areaValue()
            R.id.rd_field_size_one_acre -> areaSize = EnumFieldArea.ONE_ACRE.areaValue()
            R.id.rd_field_size_two_half_acre -> areaSize = EnumFieldArea.TWO_HALF_ACRE.areaValue()
            R.id.rd_field_size_specify_area -> {
                isExactArea = true
                areaSize = EnumFieldArea.EXACT_AREA.areaValue()
                myFieldSize = null
                return
            }
        }

        //convert to specified area unit
        val convertedAreaSize = mathHelper.convertFromAcreToSpecifiedArea(
            areaSize,
            areaUnit
        )
        saveFieldSize(convertedAreaSize)
    }

    private fun setFieldLabels(areaUnit: String) {
        val quarterAcre: String
        val halfAcre: String
        val oneAcre: String
        val twoHalfAcre: String
        binding.rdFieldSizeTwoHalfAcre.visibility = View.GONE
        when (areaUnit) {
            "acre" -> {
                quarterAcre = getString(R.string.quarter_acre)
                halfAcre = getString(R.string.half_acre)
                oneAcre = getString(R.string.one_acre)
                twoHalfAcre = getString(R.string.two_half_acres)
            }

            "ha" -> {
                quarterAcre = getString(R.string.quarter_acre_to_ha)
                halfAcre = getString(R.string.half_acre_to_ha)
                oneAcre = getString(R.string.one_acre_to_ha)
                twoHalfAcre = getString(R.string.two_half_acre_to_ha)
                binding.rdFieldSizeTwoHalfAcre.visibility = View.VISIBLE
            }

            "are" -> {
                quarterAcre = getString(R.string.quarter_acre_to_are)
                halfAcre = getString(R.string.half_acre_to_are)
                oneAcre = getString(R.string.one_acre_to_are)
                twoHalfAcre = getString(R.string.two_half_acre_to_are)
            }

            "sqm" -> {
                quarterAcre = getString(R.string.quarter_acre_to_m2)
                halfAcre = getString(R.string.half_acre_to_m2)
                oneAcre = getString(R.string.one_acre_to_m2)
                twoHalfAcre = getString(R.string.two_half_acre_to_m2)
            }

            else -> {
                quarterAcre = getString(R.string.quarter_acre)
                halfAcre = getString(R.string.half_acre)
                oneAcre = getString(R.string.one_acre)
                twoHalfAcre = getString(R.string.two_half_acres)
            }
        }


        val exactArea = requireContext().getString(R.string.exact_field_area)
        binding.apply {
            rdFieldSizeQuarterAcre.text = quarterAcre
            rdFieldSizeHalfAcre.text = halfAcre
            rdFieldSizeOneAcre.text = oneAcre
            rdFieldSizeTwoHalfAcre.text = twoHalfAcre
            rdFieldSizeSpecifyArea.text = exactArea
            fieldSizeTitle.text =
                requireContext().getString(R.string.lbl_cassava_field_size, displayAreaUnit)

        }
    }

    private fun showCustomDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.dialog_field_size)
        dialog.setCancelable(true)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT

        titleMessage = requireContext().getString(R.string.lbl_cassava_field_size, displayAreaUnit)

        // TODO: Revise these element names
        val dialogTitle = dialog.findViewById<TextView>(R.id.dialogTitle)
        val etPost = dialog.findViewById<EditText>(R.id.et_post)

        if (isExactArea && myFieldSize != null) {
            etPost.setText(myFieldSize)
        }
        dialogTitle.text = titleMessage

        dialog.findViewById<View>(R.id.bt_cancel).setOnClickListener { v: View? ->
            dialog.dismiss()
            binding.rdgFieldSize.clearCheck()
            areaSize = -1.0
            isExactArea = false
        }

        dialog.findViewById<View>(R.id.bt_submit).setOnClickListener { v: View? ->
            myFieldSize = etPost.text.toString().trim { it <= ' ' }
            if (myFieldSize!!.isEmpty()) {
                val prompt =
                    requireContext().getString(R.string.lbl_field_size_prompt, displayAreaUnit)
                Toast.makeText(context, prompt, Toast.LENGTH_SHORT).show()
            } else {
                areaSize = myFieldSize!!.toDouble()
                dialog.dismiss()
                setExactAreaText(areaSize, displayAreaUnit)
                saveFieldSize(areaSize)
                binding.txtFieldSizeSpecifiedArea.visibility = View.VISIBLE
            }
        }

        dialog.show()
        dialog.window!!.attributes = lp
    }

    private fun saveFieldSize(convertedAreaSize: Double) {
        val fieldSizeRadioIndex = binding.rdgFieldSize.checkedRadioButtonId
        if (convertedAreaSize <= 0) {
            showCustomWarningDialog(
                requireContext().getString(
                    R.string.lbl_field_size_prompt,
                    displayAreaUnit
                ), requireContext().getString(R.string.lbl_field_size_prompt, displayAreaUnit)
            )
            return
        }

        areaUnitChanged = false //Reset the area unit changed flag
        try {
            val mandatoryInfo = database.mandatoryInfoDao().findOne()
            if (mandatoryInfo != null) {
                dataValid = true
                mandatoryInfo.fieldSizeRadioIndex = fieldSizeRadioIndex
                mandatoryInfo.areaSize = convertedAreaSize
                mandatoryInfo.oldAreaUnit = areaUnit
                mandatoryInfo.exactArea = isExactArea

                database.mandatoryInfoDao().update(mandatoryInfo)
            } else {
                Toast.makeText(
                    context,
                    "Mandatory user information not provided",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (ex: Exception) {
            Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun verifyStep(): VerificationError? {
        if (areaSize <= 0 || dataValid == false) {
            errorMessage =
                requireContext().getString(R.string.lbl_field_size_prompt, displayAreaUnit)
            return VerificationError(errorMessage)
        }
        return null
    }

    override fun onSelected() {
        refreshData()
    }

    override fun onError(error: VerificationError) {
    }
}
