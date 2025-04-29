package com.akilimo.mobile.views.fragments.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.graphics.drawable.toDrawable
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.FragmentOperationCostDialogBinding
import com.akilimo.mobile.entities.OperationCost
import com.akilimo.mobile.inherit.BaseDialogFragment
import io.sentry.Sentry


/**
 * A simple [androidx.fragment.app.Fragment] subclass.
 */
class OperationCostsDialogFragment : BaseDialogFragment() {
    private var isExactCostRequired = false
    private var isPriceValid = false
    private var priceSpecified = false
    private var removeSelected = false
    private var cancelled = false

    private var selectedCost = 0.0
    private var translatedSuffix: String? = null
    private var operationName: String = ""
    private var operationType: String = ""
    private var operationCost: String? = null
    private var bagPriceRange = "NA"
    private var dialogTitle: String? = null
    private var exactPriceHint: String? = null

    private var countryCode: String = ""
    private var onDismissListener: IDismissDialog? = null

    //    private var operationCosts: ArrayList<OperationCost>? = null
    private var selectedOperationCost: OperationCost? = null

    private var _binding: FragmentOperationCostDialogBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val OPERATION_NAME: String = "operation_name"
        const val OPERATION_TYPE: String = "operation_type"
        const val COUNTRY_CODE: String = "country"
        const val CURRENCY_CODE: String = "currency_code"
        const val COST_LIST: String = "cost_list"
        const val DIALOG_TITLE: String = "dialog_title"
        const val EXACT_PRICE_HINT: String = "exact_price_title"

        private val LOG_TAG: String = OperationCostsDialogFragment::class.java.simpleName

        const val ARG_ITEM_ID: String = "fertilizer_dialog_fragment"
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bundle = this.arguments
        val context = requireContext()
        bundle?.let {
            countryCode = it.getString(COUNTRY_CODE) ?: ""
            currencySymbol = it.getString(CURRENCY_CODE) ?: ""
            operationName = it.getString(OPERATION_NAME) ?: ""
            operationType = it.getString(OPERATION_TYPE) ?: ""
            dialogTitle = it.getString(DIALOG_TITLE)
            exactPriceHint = it.getString(EXACT_PRICE_HINT)
        }

        _binding = FragmentOperationCostDialogBinding.inflate(layoutInflater)

        val dialog = Dialog(context)

        dialog.apply {
            window!!.requestFeature(Window.FEATURE_NO_TITLE)
            window!!.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            )
            window!!.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            window!!.attributes.windowAnimations = R.style.DialogSlideAnimation
            setCancelable(true)

            setContentView(binding.root)
        }


        translatedSuffix = context.getString(R.string.lbl_to)

        var titleText = context.getString(R.string.lbl_operation_cost)
        if (!dialogTitle.isNullOrEmpty()) {
            titleText = dialogTitle!!
        }
        binding.lblFragmentTitle.text = titleText


        binding.closeButton.setOnClickListener { view: View? ->
            priceSpecified = false
            removeSelected = false
            cancelled = true
            dismiss()
        }

        //save the data
        binding.saveButton.setOnClickListener {
            if (isExactCostRequired) {
                operationCost = binding.editExactCost.getText().toString()
                if (operationCost.isNullOrEmpty()) {
                    binding.editExactCost.error = "Please provide a valid cost value"
                    isPriceValid = false
                    return@setOnClickListener
                }
                selectedCost = operationCost!!.toDouble()
                bagPriceRange = mathHelper.formatNumber(selectedCost, currencySymbol)
                isPriceValid = true
                cancelled = false
                binding.editExactCost.error = null
            }
            if (isPriceValid) {
                priceSpecified = true
                removeSelected = false
                dismiss()
            }
        }

        binding.radioGroup.setOnCheckedChangeListener { radioGroup: RadioGroup, _: Int ->
            radioSelected(radioGroup, dialog)
        }
        addCostRadioButtons()
        binding.exactPriceWrapper.hint = exactPriceHint
        return dialog
    }

    private fun radioSelected(radioGroup: RadioGroup, dialog: Dialog) {
        val radioButtonId = radioGroup.checkedRadioButtonId
        val radioButton = dialog.findViewById<RadioButton>(radioButtonId)
        val itemTagIndex = radioButton.tag as String
        selectedOperationCost = database.operationCostDao().findOneByItemTag(itemTagIndex)
        try {
            selectedCost = selectedOperationCost!!.averageCost
            operationCost = selectedCost.toString()
            isExactCostRequired = false
            isPriceValid = true

            if (selectedCost < 0) {
                isExactCostRequired = true
                isPriceValid = false
                binding.exactPriceWrapper.visibility = View.VISIBLE
            } else {
                binding.exactPriceWrapper.visibility = View.GONE
                binding.exactPriceWrapper.editText?.text = null
            }
        } catch (ex: Exception) {
            Sentry.captureException(ex)
        }
    }

    private fun addCostRadioButtons() {
        binding.radioGroup.removeAllViews()
        val context = requireContext()
        val operationCosts = database.operationCostDao()
            .findAllFiltered(operationName, operationType, countryCode)
        for (cost in operationCosts) {
            val listIndex = cost.itemTag
            val price = cost.averageCost

            val radioButton = RadioButton(activity)
            radioButton.id = View.generateViewId()
            radioButton.tag = listIndex


            val defaultLabel = String.format(
                getString(R.string.lbl_operation_cost_label),
                mathHelper.formatNumber(price, null),
                currencySymbol
            )

            val radioLabel = when {
                price == 0.0 -> context.getString(R.string.lbl_do_not_know)
                price < 0 -> context.getString(R.string.exact_fertilizer_price)
                else -> defaultLabel
            }
            radioButton.text = radioLabel

            binding.radioGroup.addView(radioButton)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (onDismissListener != null) {
            onDismissListener!!.onDismiss(
                selectedOperationCost,
                operationName,
                selectedCost,
                cancelled,
                isExactCostRequired
            )
        }
    }

    fun setOnDismissListener(dismissListener: IDismissDialog?) {
        this.onDismissListener = dismissListener
    }

    interface IDismissDialog {
        fun onDismiss(
            operationCost: OperationCost?,
            operationName: String?,
            selectedCost: Double,
            cancelled: Boolean,
            isExactCost: Boolean
        )
    }
}
