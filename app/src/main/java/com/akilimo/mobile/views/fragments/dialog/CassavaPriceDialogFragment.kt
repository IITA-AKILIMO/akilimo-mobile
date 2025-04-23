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


    private var maxPrice = 0.0
    private var minPrice = 0.0
    private var isExactPriceRequired = false
    private var isPriceValid = false
    private var dialog: Dialog? = null
    private var cassavaPriceList: List<CassavaPrice>? = null
    private var countryCode: String? = null
    private var currencyName: String? = null
    private var currencyCode: String? = null
    private var unitOfSale: String? = null
    private var unitOfSaleEnum: EnumUnitOfSale? = null
    private var averagePrice: Double = 0.0
    private var unitPrice: Double = 0.0
    private var minAmountUSD = 5.00
    private var onDismissListener: IPriceDialogDismissListener? = null

    private var _binding: FragmentCassavaPriceDialogBinding? = null
    private val binding get() = _binding!!

    companion object {
        private val LOG_TAG: String = CassavaPriceDialogFragment::class.java.simpleName
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
            averagePrice = bundle.getDouble(AVERAGE_PRICE)
            unitPrice = bundle.getDouble(SELECTED_PRICE)
            currencyCode = bundle.getString(CURRENCY_CODE)
            unitOfSale = bundle.getString(UNIT_OF_SALE)
            countryCode = bundle.getString(COUNTRY_CODE)
            unitOfSaleEnum = bundle.getParcelable(ENUM_UNIT_OF_SALE)

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
                    try {
                        unitPrice = editExactFertilizerPrice.text.toString().toDouble()
                    } catch (ex: Exception) {
                        Sentry.captureException(ex)
                        unitPrice = 0.0
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

        if (database != null) {
            cassavaPriceList = database!!.cassavaPriceDao().findAllByCountryCode(countryCode!!)
            addPriceRadioButtons(cassavaPriceList!!, averagePrice)
        }

        return dialog
    }

    private fun radioSelected(radioGroup: RadioGroup, dialog: Dialog) {
        val radioButtonId = radioGroup.checkedRadioButtonId
        val radioButton = dialog.findViewById<RadioButton>(radioButtonId)
        val itemTagIndex = radioButton.tag as Long

        try {
            val pricesResp = cassavaPriceList!![itemTagIndex.toInt()]
            isExactPriceRequired = false
            isPriceValid = true
            averagePrice = pricesResp.averagePrice
            binding.exactPriceWrapper?.visibility = View.GONE
            binding.editExactFertilizerPrice?.text = null

            if (averagePrice < 0) {
                isExactPriceRequired = true
                isPriceValid = false
                binding.exactPriceWrapper?.visibility = View.VISIBLE
            } else {
                unitPrice = pricesResp.averagePrice
            }
        } catch (ex: Exception) {
            Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
            Sentry.captureException(ex)
        }
    }

    private fun addPriceRadioButtons(cassavaPriceList: List<CassavaPrice>, avgPrice: Double) {
        binding.radioGroup?.removeAllViews()
        val selectedPrice = 0.0

        if (avgPrice < 0) {
            binding.updateButton?.setText(R.string.lbl_update)
            binding.removeButton?.visibility = View.VISIBLE
        }

        for ((id, _, minLocalPrice, maxLocalPrice, minAllowedPrice, maxAllowedPrice, _, price) in cassavaPriceList) {
            minPrice = minAllowedPrice
            maxPrice = maxAllowedPrice

            val listIndex = (id - 1).toLong() //reduce by one so as to match the index in the list

            val radioButton = RadioButton(activity)
            radioButton.id = View.generateViewId()
            radioButton.tag = listIndex

            var radioLabel = labelText(minLocalPrice, maxLocalPrice, currencyCode, unitOfSale)
            if (price < 0) {
                radioLabel = requireContext().getString(R.string.lbl_exact_price_x_per_unit_of_sale)
                val exactTextHint =
                    getString(R.string.exact_fertilizer_price_currency, currencyName)
                binding.exactPriceWrapper?.hint = exactTextHint
            } else if (price == 0.0) {
                radioLabel = requireContext().getString(R.string.lbl_do_not_know)
            }
            radioButton.text = radioLabel
            binding.radioGroup?.addView(radioButton)

            if (avgPrice < 0) {
                radioButton.isChecked = true
                isPriceValid = true
                isExactPriceRequired = true
                binding.exactPriceWrapper?.visibility = View.VISIBLE
                binding.editExactFertilizerPrice?.setText(unitPrice.toString())
            }

            if (price == selectedPrice) {
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
        unitPriceLower: Double,
        unitPriceUpper: Double,
        currencyCode: String?,
        uos: String?
    ): String {
        var priceLower = unitPriceLower
        var priceHigher = unitPriceUpper
        var currencySymbol = currencyCode
        val extendedCurrency = CurrencyCode.getCurrencySymbol(currencyCode)
        if (extendedCurrency != null) {
            currencySymbol = extendedCurrency.symbol
        }
        when (unitOfSaleEnum) {
            EnumUnitOfSale.ONE_KG -> {
                priceLower = (unitPriceLower * EnumUnitOfSale.ONE_KG.unitWeight()) / 1000
                priceHigher = (unitPriceUpper * EnumUnitOfSale.ONE_KG.unitWeight()) / 1000
            }

            EnumUnitOfSale.FIFTY_KG -> {
                priceLower = (unitPriceLower * EnumUnitOfSale.FIFTY_KG.unitWeight()) / 1000
                priceHigher = (unitPriceUpper * EnumUnitOfSale.FIFTY_KG.unitWeight()) / 1000
            }

            EnumUnitOfSale.HUNDRED_KG -> {
                priceLower = (unitPriceLower * EnumUnitOfSale.HUNDRED_KG.unitWeight()) / 1000
                priceHigher = (unitPriceUpper * EnumUnitOfSale.HUNDRED_KG.unitWeight()) / 1000
            }

            EnumUnitOfSale.NA -> TODO()
            EnumUnitOfSale.FRESH_COB -> TODO()
            EnumUnitOfSale.THOUSAND_KG -> TODO()
            null -> TODO()
        }

        minAmountUSD = priceLower
        return requireContext().getString(
            R.string.unit_price_label_single,
            priceLower,
            currencySymbol,
            uos
        )
    }
}
