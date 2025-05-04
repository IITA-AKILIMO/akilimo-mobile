package com.akilimo.mobile.views.fragments.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.FragmentCassavaPriceDialogBinding
import com.akilimo.mobile.entities.CassavaPrice
import com.akilimo.mobile.inherit.BaseDialogFragment
import com.akilimo.mobile.interfaces.IPriceDialogDismissListener
import com.akilimo.mobile.utils.CurrencyCode
import com.akilimo.mobile.utils.enums.EnumUnitOfSale
import io.sentry.Sentry

class CassavaPriceDialogFragment : BaseDialogFragment() {
    private var isExactPriceRequired = false
    private var isPriceValid = false
    private var cassavaPriceList: List<CassavaPrice>? = null
    private var countryCode: String? = null
    private var currencyName: String = "USD"
    private var currencyCode: String = "USD"
    private var unitOfSale: String = "NA"
    private var averagePrice: Double = 0.0
    private var unitPrice: Double = 0.0
    private var onDismissListener: IPriceDialogDismissListener? = null

    private var _binding: FragmentCassavaPriceDialogBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val ARG_ITEM_ID: String = "cassava_price_dialog_fragment"
        const val AVERAGE_PRICE: String = "average_price"
        const val SELECTED_PRICE: String = "selected_price"
        const val UNIT_OF_SALE: String = "unit_of_sale"
        const val ENUM_UNIT_OF_SALE: String = "enum_unit_of_sale"
        const val CURRENCY_CODE: String = "currency_code"
        const val COUNTRY_CODE: String = "country_code"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bundle = this.arguments
        if (bundle != null) {
            bundle.apply {
                averagePrice = getDouble(AVERAGE_PRICE)
                unitPrice = getDouble(SELECTED_PRICE)
                currencyCode = getString(CURRENCY_CODE) ?: "USD"
                unitOfSale = getString(UNIT_OF_SALE) ?: "NA"
                countryCode = getString(COUNTRY_CODE)
            }

            val extendedCurrency = CurrencyCode.getCurrencySymbol(currencyCode)
            if (extendedCurrency != null) {
                currencyName = extendedCurrency.name
            }
        }

        _binding = FragmentCassavaPriceDialogBinding.inflate(layoutInflater)

        val dialog = Dialog(requireContext())

        dialog.apply {
            window!!.requestFeature(Window.FEATURE_NO_TITLE)
            window!!.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            )

            setCancelable(true)
            window!!.setBackgroundDrawableResource(android.R.color.transparent)
            window!!.attributes.windowAnimations = R.style.DialogSlideAnimation

            setContentView(binding.root)
        }

        binding.apply {
            closeButton.setOnClickListener {
                dismiss()
            }

            removeButton.setOnClickListener {
                averagePrice = 0.0
                dismiss()
            }

            updateButton.setOnClickListener {
                if (isExactPriceRequired) {
                    unitPrice = try {
                        editExactFertilizerPrice.text.toString().toDouble()
                    } catch (ex: Exception) {
                        0.0
                    }
                    if (unitPrice <= 0) {
                        editExactFertilizerPrice.error =
                            getString(R.string.lbl_provide_valid_unit_price)
                        isPriceValid = false
                        return@setOnClickListener
                    }
                    isPriceValid = true
                    editExactFertilizerPrice.error = null
                }
                if (isPriceValid) {
                    dismiss()
                }
            }

            radioGroup.setOnCheckedChangeListener { group, _ ->
                radioSelected(group, dialog)
            }
        }
        cassavaPriceList = database.cassavaPriceDao().findAllByCountryCode(countryCode!!)
        addPriceRadioButtons(cassavaPriceList!!, averagePrice)

        return dialog
    }

    private fun radioSelected(radioGroup: RadioGroup, dialog: Dialog) {
        val radioButtonId = radioGroup.checkedRadioButtonId
        val radioButton = dialog.findViewById<RadioButton>(radioButtonId)
        val radioTag = radioButton.tag as String

        try {
            val pricesResp = database.cassavaPriceDao().findByItemTag(radioTag)
            if (pricesResp != null) {
                isExactPriceRequired = false
                isPriceValid = true
                averagePrice = pricesResp.averagePrice
                unitPrice = pricesResp.averagePrice
                binding.exactPriceWrapper.visibility = View.GONE
                binding.editExactFertilizerPrice.visibility = View.GONE
                binding.editExactFertilizerPrice.text = null
            }

            if (radioTag.contains("exact", true)) {
                isExactPriceRequired = true
                isPriceValid = false
                binding.exactPriceWrapper.visibility = View.VISIBLE
                binding.editExactFertilizerPrice.visibility = View.VISIBLE
            }

        } catch (ex: Exception) {
            Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
            Sentry.captureException(ex)
        }
    }

    private fun addPriceRadioButtons(cassavaPriceList: List<CassavaPrice>, selectedPrice: Double) {
        binding.radioGroup.removeAllViews()

        if (selectedPrice < 0.0) {
            binding.updateButton.setText(R.string.lbl_update)
            binding.removeButton.visibility = View.VISIBLE
        }

        for (priceList in cassavaPriceList) {
            val averagePrice = computeUnitPrice(priceList.averagePrice, unitOfSale)
            val radioTag = priceList.itemTag


            val radioButton = RadioButton(activity)
            radioButton.id = View.generateViewId()

            var radioLabel = labelText(averagePrice, currencyCode, unitOfSale)
            if (averagePrice < 0.0) {
                radioLabel = requireContext().getString(R.string.lbl_exact_price_x_per_unit_of_sale)
                val exactTextHint =
                    getString(R.string.exact_fertilizer_price_currency, currencyName)
                binding.exactPriceWrapper.hint = exactTextHint
            } else if (averagePrice == 0.0) {
                radioLabel = requireContext().getString(R.string.lbl_do_not_know)
            }

            radioButton.tag = radioTag
            radioButton.text = radioLabel
            binding.radioGroup.addView(radioButton)

            if (selectedPrice < 0.0 && radioTag.contains("exact", true)) {
                radioButton.isChecked = true
                isPriceValid = true
                isExactPriceRequired = true
                binding.exactPriceWrapper.visibility = View.VISIBLE
                binding.editExactFertilizerPrice.setText(unitPrice.toString())
            }

            if (averagePrice.equals(selectedPrice)) {
                radioButton.isChecked = true
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.onDismiss(unitPrice, isExactPriceRequired)
    }

    fun setOnDismissListener(dismissListener: IPriceDialogDismissListener?) {
        this.onDismissListener = dismissListener
    }

    private fun labelText(
        computedAveragePrice: Double,
        currencyCode: String,
        unitOfSale: String
    ): String {
        var currencySymbol = currencyCode
        val extendedCurrency = CurrencyCode.getCurrencySymbol(currencyCode)
        if (extendedCurrency != null) {
            currencySymbol = extendedCurrency.symbol
        }
        val unitOfSaleEnum = EnumUnitOfSale.valueOf(unitOfSale)
        return requireContext().getString(
            R.string.unit_price_label_single,
            computedAveragePrice,
            currencySymbol,
            unitOfSaleEnum.unitOfSale(requireContext())
        )
    }

    private fun computeUnitPrice(avgPrice: Double, unitOfSale: String): Double {
        var computedAveragePrice = avgPrice
        val unitOfSaleEnum = EnumUnitOfSale.valueOf(unitOfSale)
        when (unitOfSaleEnum) {
            EnumUnitOfSale.ONE_KG -> {
                computedAveragePrice =
                    (avgPrice * EnumUnitOfSale.ONE_KG.unitWeight()) / 1000
            }

            EnumUnitOfSale.FIFTY_KG -> {
                computedAveragePrice =
                    (avgPrice * EnumUnitOfSale.FIFTY_KG.unitWeight()) / 1000
            }

            EnumUnitOfSale.HUNDRED_KG -> {
                computedAveragePrice =
                    (avgPrice * EnumUnitOfSale.HUNDRED_KG.unitWeight()) / 1000
            }

            EnumUnitOfSale.TONNE -> {
                computedAveragePrice =
                    (avgPrice * EnumUnitOfSale.TONNE.unitWeight()) / 1000
            }

            EnumUnitOfSale.NA, EnumUnitOfSale.FRESH_COB -> {
                computedAveragePrice = avgPrice
            }
        }
        return computedAveragePrice
    }
}
