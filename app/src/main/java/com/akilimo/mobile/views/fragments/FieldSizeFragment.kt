package com.akilimo.mobile.views.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.FragmentFieldSizeBinding
import com.akilimo.mobile.entities.MandatoryInfo
import com.akilimo.mobile.entities.ProfileInfo
import com.akilimo.mobile.inherit.BaseStepFragment
import com.akilimo.mobile.utils.enums.EnumFieldArea
import com.stepstone.stepper.VerificationError
import io.sentry.Sentry


/**
 * A simple [Fragment] subclass.
 * Use the [FieldSizeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FieldSizeFragment : BaseStepFragment() {
    var binding: FragmentFieldSizeBinding? = null

    var title: AppCompatTextView? = null
    var specifiedArea: AppCompatTextView? = null
    var rdgFieldArea: RadioGroup? = null
    var rdSpecifyArea: RadioButton? = null
    var rd_quarter_acre: RadioButton? = null
    var rd_half_acre: RadioButton? = null
    var rd_one_acre: RadioButton? = null
    var rd_two_half_acre: RadioButton? = null

    private var profileInfo: ProfileInfo? = null
    private var mandatoryInfo: MandatoryInfo? = null

    private var myFieldSize: String? = ""
    private var areaSize = 0.0
    private var isExactArea = false
    private var areaUnitChanged = false
    private var titleMessage: String? = null
    private var areaUnit: String? = ""
    private var displayLanguage: String? = ""
    private var displayAreaUnit: String? = ""
    private var oldAreaUnit: String? = ""
    private var fieldSizeRadioIndex = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }

    override fun loadFragmentLayout(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle
    ): View {
        binding = FragmentFieldSizeBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = binding!!.title
        specifiedArea = binding!!.specifiedArea
        rdgFieldArea = binding!!.rdgFieldArea
        rdSpecifyArea = binding!!.rdSpecifyAcre
        rd_quarter_acre = binding!!.rdQuarterAcre
        rd_half_acre = binding!!.rdHalfAcre
        rd_one_acre = binding!!.rdOneAcre
        rd_two_half_acre = binding!!.rdTwoHalfAcre


        rdgFieldArea!!.setOnCheckedChangeListener { radioGroup: RadioGroup?, radioIndex: Int ->
            val radioButton = view.findViewById<RadioButton>(radioIndex)
            if (radioButton != null) {
                if (radioButton.isPressed) {
                    radioSelected(radioIndex)
                }
            }
        }

        rdSpecifyArea!!.setOnClickListener { radioButton: View? ->
            if (radioButton != null && radioButton.isPressed) {
                showCustomDialog()
            }
        }
    }

    fun refreshData() {
        try {
            profileInfo = database.profileInfoDao().findOne()
            if (profileInfo != null) {
                displayLanguage = profileInfo!!.language
            }
            mandatoryInfo = database.mandatoryInfoDao().findOne()
            if (mandatoryInfo != null) {
                isExactArea = mandatoryInfo!!.exactArea
                areaUnit = mandatoryInfo!!.areaUnit
                displayAreaUnit = mandatoryInfo!!.displayAreaUnit
                oldAreaUnit = mandatoryInfo!!.oldAreaUnit
                areaSize = mandatoryInfo!!.areaSize
                fieldSizeRadioIndex = mandatoryInfo!!.fieldSizeRadioIndex
                myFieldSize = areaSize.toString()
                areaUnitChanged = !areaUnit.equals(oldAreaUnit, ignoreCase = true)

                setFieldLabels(areaUnit!!)
                if (areaUnitChanged) {
                    areaSize = 0.0
                    myFieldSize = null
                    rdgFieldArea!!.clearCheck()
                } else {
                    rdgFieldArea!!.check(fieldSizeRadioIndex)
                }
                if (isExactArea) {
                    setExactAreaText(areaSize, displayAreaUnit!!)
                    specifiedArea!!.visibility = View.VISIBLE
                } else {
                    specifiedArea!!.visibility = View.GONE
                }
            }
        } catch (ex: Exception) {
            Sentry.captureException(ex)
        }
    }

    private fun setExactAreaText(areaSize: Double, displayUnit: String) {
        val fieldSize = mathHelper.removeLeadingZero(areaSize)
        var areaUnitLabel = String.format("%s %s", fieldSize, displayUnit)
        if (displayLanguage.equals("sw", ignoreCase = true)) {
            areaUnitLabel = String.format("%s %s", displayUnit, fieldSize)
        }
        specifiedArea!!.text = areaUnitLabel
    }


    private fun radioSelected(checked: Int) {
        specifiedArea!!.visibility = View.GONE
        isExactArea = false
        when (checked) {
            R.id.rd_quarter_acre -> areaSize = EnumFieldArea.QUARTER_ACRE.areaValue()
            R.id.rd_half_acre -> areaSize = EnumFieldArea.HALF_ACRE.areaValue()
            R.id.rd_one_acre -> areaSize = EnumFieldArea.ONE_ACRE.areaValue()
            R.id.rd_two_half_acre -> areaSize = EnumFieldArea.TWO_HALF_ACRE.areaValue()
            R.id.rd_specify_acre -> {
                isExactArea = true
                areaSize = EnumFieldArea.EXACT_AREA.areaValue()
                myFieldSize = null
                return
            }
        }

        //convert to specified area unit
        val convertedAreaSize = mathHelper.convertFromAcreToSpecifiedArea(
            areaSize,
            areaUnit!!
        )
        saveFieldSize(convertedAreaSize)
    }

    private fun setFieldLabels(areaUnit: String) {
        val quarterAcre: String
        val halfAcre: String
        val oneAcre: String
        val twoHalfAcre: String
        rd_two_half_acre!!.visibility = View.GONE
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
                rd_two_half_acre!!.visibility = View.VISIBLE
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


        val exactArea = context.getString(R.string.exact_field_area)
        rd_quarter_acre!!.text = quarterAcre
        rd_half_acre!!.text = halfAcre
        rd_one_acre!!.text = oneAcre
        rd_two_half_acre!!.text = twoHalfAcre
        rdSpecifyArea!!.text = exactArea
        titleMessage = context.getString(R.string.lbl_cassava_field_size, displayAreaUnit)
        title!!.text = titleMessage
    }

    private fun showCustomDialog() {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.dialog_field_size)
        dialog.setCancelable(true)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT

        titleMessage = context.getString(R.string.lbl_cassava_field_size, displayAreaUnit)

        val dialogTitle = dialog.findViewById<TextView>(R.id.dialogTitle)
        val et_post = dialog.findViewById<EditText>(R.id.et_post)

        if (isExactArea && myFieldSize != null) {
            et_post.setText(myFieldSize)
        }
        dialogTitle.text = titleMessage

        dialog.findViewById<View>(R.id.bt_cancel).setOnClickListener { v: View? ->
            dialog.dismiss()
            rdgFieldArea!!.clearCheck()
            areaSize = -1.0
            isExactArea = false
        }

        dialog.findViewById<View>(R.id.bt_submit).setOnClickListener { v: View? ->
            myFieldSize = et_post.text.toString().trim { it <= ' ' }
            if (myFieldSize!!.isEmpty()) {
                val prompt =
                    context.getString(R.string.lbl_field_size_prompt, displayAreaUnit)
                Toast.makeText(context, prompt, Toast.LENGTH_SHORT).show()
            } else {
                areaSize = myFieldSize!!.toDouble()
                dialog.dismiss()
                setExactAreaText(areaSize, displayAreaUnit!!)
                saveFieldSize(areaSize)
                specifiedArea!!.visibility = View.VISIBLE
            }
        }

        dialog.show()
        dialog.window!!.attributes = lp
    }

    private fun saveFieldSize(convertedAreaSize: Double) {
        fieldSizeRadioIndex = rdgFieldArea!!.checkedRadioButtonId
        if (convertedAreaSize <= 0) {
            showCustomWarningDialog(
                context.getString(
                    R.string.lbl_field_size_prompt,
                    displayAreaUnit
                ), context.getString(R.string.lbl_field_size_prompt, displayAreaUnit)
            )
            return
        }

        areaUnitChanged = false //Reset the area unit changed flag
        try {
            if (mandatoryInfo == null) {
                mandatoryInfo = MandatoryInfo()
            }
            mandatoryInfo!!.fieldSizeRadioIndex = fieldSizeRadioIndex
            mandatoryInfo!!.areaSize = convertedAreaSize
            mandatoryInfo!!.oldAreaUnit = areaUnit
            mandatoryInfo!!.exactArea = isExactArea
            if (mandatoryInfo!!.id != null) {
                database.mandatoryInfoDao().update(mandatoryInfo!!)
            } else {
                database.mandatoryInfoDao().insert(mandatoryInfo!!)
            }
            mandatoryInfo = database.mandatoryInfoDao().findOne()
        } catch (ex: Exception) {
            Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun verifyStep(): VerificationError? {
        if (!(areaSize <= 0)) {
            return null
        }
        errorMessage = context.getString(R.string.lbl_field_size_prompt, displayAreaUnit)
        return VerificationError(errorMessage)
    }

    override fun onSelected() {
        refreshData()
    }

    override fun onError(error: VerificationError) {
    }

    companion object {
        fun newInstance(): FieldSizeFragment {
            return FieldSizeFragment()
        }
    }
}
