package com.akilimo.mobile.views.fragments.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.FragmentOperationCostDialogBinding
import com.akilimo.mobile.entities.OperationCost
import com.akilimo.mobile.inherit.BaseBottomSheetDialogFragment
import io.sentry.Sentry

class OperationCostsDialogFragment : BaseBottomSheetDialogFragment() {

    private var _binding: FragmentOperationCostDialogBinding? = null
    private val binding get() = _binding!!

    private var selectedOperationCost: OperationCost? = null
    private var operationCost: String? = null
    private var translatedSuffix: String? = null

    private var operationName: String = ""
    private var operationType: String = ""
    private var bagPriceRange = "NA"
    private var dialogTitle: String? = null
    private var exactPriceHint: String? = null
    private var countryCode: String = ""

    private var isExactCostRequired = false
    private var isPriceValid = false
    private var priceSpecified = false
    private var removeSelected = false
    private var cancelled = false

    private var selectedCost = 0.0
    private var onDismissListener: IDismissDialog? = null

    companion object {
        const val ARG_ITEM_ID: String = "fertilizer_dialog_fragment"
        const val OPERATION_NAME = "operation_name"
        const val OPERATION_TYPE = "operation_type"
        const val COUNTRY_CODE = "country"
        const val CURRENCY_CODE = "currency_code"
        const val DIALOG_TITLE = "dialog_title"
        const val EXACT_PRICE_HINT = "exact_price_title"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOperationCostDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            countryCode = it.getString(COUNTRY_CODE) ?: ""
            currencySymbol = it.getString(CURRENCY_CODE) ?: ""
            operationName = it.getString(OPERATION_NAME) ?: ""
            operationType = it.getString(OPERATION_TYPE) ?: ""
            dialogTitle = it.getString(DIALOG_TITLE)
            exactPriceHint = it.getString(EXACT_PRICE_HINT)
        }

        translatedSuffix = requireContext().getString(R.string.lbl_to)
        binding.lblFragmentTitle.text = dialogTitle ?: getString(R.string.lbl_operation_cost)
        binding.exactPriceWrapper.hint = exactPriceHint

        binding.closeButton.setOnClickListener {
            priceSpecified = false
            removeSelected = false
            cancelled = true
            dismiss()
        }

        binding.saveButton.setOnClickListener {
            if (isExactCostRequired) {
                operationCost = binding.editExactCost.text.toString()
                if (operationCost.isNullOrEmpty()) {
                    binding.editExactCost.error = getString(R.string.lbl_invalid_cost)
                    isPriceValid = false
                    return@setOnClickListener
                }
                selectedCost = operationCost!!.toDouble()
                bagPriceRange = mathHelper.formatNumber(selectedCost, currencySymbol)
                isPriceValid = true
                cancelled = false
            }
            if (isPriceValid) {
                priceSpecified = true
                removeSelected = false
                dismiss()
            }
        }

        binding.radioGroup.setOnCheckedChangeListener { group, _ ->
            handleRadioSelection(group)
        }

        addCostRadioButtons()
    }

    private fun handleRadioSelection(radioGroup: RadioGroup) {
        val selectedId = radioGroup.checkedRadioButtonId
        val selectedRadio = radioGroup.findViewById<RadioButton>(selectedId)
        val itemTag = selectedRadio.tag as String

        selectedOperationCost = database.operationCostDao().findOneByItemTag(itemTag)

        try {
            selectedCost = selectedOperationCost?.averageCost ?: 0.0
            operationCost = selectedCost.toString()
            isExactCostRequired = selectedCost < 0
            isPriceValid = selectedCost >= 0

            binding.exactPriceWrapper.visibility =
                if (isExactCostRequired) View.VISIBLE else View.GONE
            if (!isExactCostRequired) binding.editExactCost.setText("")
        } catch (ex: Exception) {
            Sentry.captureException(ex)
        }
    }

    private fun addCostRadioButtons() {
        val context = requireContext()
        val operationCosts = database.operationCostDao()
            .findAllFiltered(operationName, operationType, countryCode)

        binding.radioGroup.removeAllViews()
        for (cost in operationCosts) {
            val radioButton = RadioButton(context).apply {
                id = View.generateViewId()
                tag = cost.itemTag

                val label = when {
                    cost.averageCost == 0.0 -> context.getString(R.string.lbl_do_not_know)
                    cost.averageCost < 0 -> context.getString(R.string.lbl_exact_cost)
                    else -> context.getString(
                        R.string.lbl_operation_cost_label,
                        mathHelper.formatNumber(cost.averageCost, null),
                        currencySymbol
                    )
                }
                text = label
            }
            binding.radioGroup.addView(radioButton)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.onDismiss(
            selectedOperationCost,
            operationName,
            selectedCost,
            cancelled,
            isExactCostRequired
        )
    }

    fun setOnDismissListener(listener: IDismissDialog?) {
        onDismissListener = listener
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
