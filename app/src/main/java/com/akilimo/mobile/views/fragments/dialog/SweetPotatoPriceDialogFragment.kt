package com.akilimo.mobile.views.fragments.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.FragmentCassavaPriceDialogBinding
import com.akilimo.mobile.entities.PotatoPrice
import com.akilimo.mobile.interfaces.IPriceDialogDismissListener
import com.akilimo.mobile.utils.CurrencyCode
import com.akilimo.mobile.utils.MathHelper
import com.akilimo.mobile.utils.enums.EnumUnitOfSale
import io.sentry.Sentry

class SweetPotatoPriceDialogFragment : DialogFragment() {
    private var isExactPriceRequired = false
    private var isPriceValid = false
    private val priceSpecified = false
    private val removeSelected = false

    private var dialog: Dialog? = null
    private lateinit var binding: FragmentCassavaPriceDialogBinding

    private val mathHelper: MathHelper? = null
    private var averagePrice = 0.0
    private var potatoPrice = 0.0
    private var potatoPriceList: List<PotatoPrice>? = null

    private var countryCode: String? = null
    private var currencyCode: String? = null
    private var unitOfSale: String? = null
    private var enumUnitOfSale: EnumUnitOfSale? = null

    var unitPriceUSD: Double = 0.0
    var unitPriceLocal: Double = 0.0
    private var minAmountUSD = 5.00
    private val maxAmountUSD = 500.00

    private var onDismissListener: IPriceDialogDismissListener? = null

    companion object {
        private val LOG_TAG: String = SweetPotatoPriceDialogFragment::class.java.simpleName
        const val ARG_ITEM_ID: String = "sweet_potato_price_dialog_fragment"
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
            potatoPrice = bundle.getDouble(SELECTED_PRICE)
            currencyCode = bundle.getString(CURRENCY_CODE)
            unitOfSale = bundle.getString(UNIT_OF_SALE)
            countryCode = bundle.getString(COUNTRY_CODE)
            enumUnitOfSale = bundle.getParcelable(ENUM_UNIT_OF_SALE)
        }

        dialog = Dialog(requireContext())
        binding = FragmentCassavaPriceDialogBinding.inflate(layoutInflater)

        dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog!!.window!!.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        )
        dialog!!.setContentView(binding.root)

        dialog!!.setCancelable(true)
        dialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog!!.window!!.attributes.windowAnimations = R.style.DialogSlideAnimation

        binding.closeButton.setOnClickListener {
            dismiss()
        }

        binding.removeButton.setOnClickListener {
            averagePrice = 0.0
            dismiss()
        }

        // Save the data
        binding.updateButton.setOnClickListener {
            if (isExactPriceRequired) {
                try {
                    potatoPrice = binding.editExactFertilizerPrice.text.toString().toDouble()
                } catch (ex: Exception) {
                    Sentry.captureException(ex)
                }
                if (potatoPrice <= 0) {
                    binding.editExactFertilizerPrice.error =
                        getString(R.string.lbl_provide_valid_unit_price)
                    isPriceValid = false
                    return@setOnClickListener
                }
                isPriceValid = true
                binding.editExactFertilizerPrice.error = null
            }
            if (isPriceValid) {
                dismiss()
            }
        }

        binding.radioGroup.setOnCheckedChangeListener { radioGroup, i ->
            radioSelected(radioGroup)
        }

        potatoPriceList?.let {
            addPriceRadioButtons(it, averagePrice)
        }

        return dialog!!
    }

    private fun radioSelected(radioGroup: RadioGroup) {
        val radioButtonId = radioGroup.checkedRadioButtonId
        val radioButton = dialog!!.findViewById<RadioButton>(radioButtonId)
        val itemTagIndex = radioButton.tag as Long

        try {
            val pricesResp = potatoPriceList!![itemTagIndex.toInt()]
            isExactPriceRequired = false
            isPriceValid = true
            averagePrice = pricesResp.averagePrice
            binding.exactPriceWrapper.visibility = View.GONE
            binding.exactPriceWrapper.editText?.text = null

            if (averagePrice < 0) {
                isExactPriceRequired = true
                isPriceValid = false
                binding.exactPriceWrapper.visibility = View.VISIBLE
            } else {
                potatoPrice = pricesResp.averagePrice
            }
        } catch (ex: Exception) {
            Sentry.captureException(ex)
        }
    }

    private fun addPriceRadioButtons(potatoPriceList: List<PotatoPrice>, avgPrice: Double) {
        val context = requireContext()
        binding.radioGroup.removeAllViews()
        val selectedPrice = 0.0

        if (avgPrice < 0) {
            binding.updateButton.setText(R.string.lbl_update)
            binding.removeButton.visibility = View.VISIBLE
        }

        for ((id, _, _, _, minLocalPrice, maxLocalPrice, _, _, _, _, price) in potatoPriceList) {
            val listIndex = id - 1 // Reduce by one so as to match the index in the list

            val radioButton = RadioButton(activity)
            radioButton.id = View.generateViewId()
            radioButton.tag = listIndex

            var radioLabel = labelText(minLocalPrice, maxLocalPrice, currencyCode, unitOfSale)
            if (price < 0) {
                radioLabel = context.getString(R.string.lbl_exact_price_x_per_unit_of_sale)
            } else if (price == 0.0) {
                radioLabel = context.getString(R.string.lbl_do_not_know)
            }
            radioButton.text = radioLabel
            binding.radioGroup.addView(radioButton)

            // Set relevant radio button as selected based on the price range
            if (avgPrice < 0) {
                radioButton.isChecked = true
                isPriceValid = true
                isExactPriceRequired = true
                binding.exactPriceWrapper.visibility = View.VISIBLE
                binding.editExactFertilizerPrice.setText(potatoPrice.toString())
            }

            if (price == selectedPrice) {
                radioButton.isChecked = true
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.onDismiss(potatoPrice, isExactPriceRequired)
    }

    fun setOnDismissListener(dismissListener: IPriceDialogDismissListener?) {
        this.onDismissListener = dismissListener
    }

    private fun labelText(
        unitPriceLower: Double,
        unitPriceUpper: Double,
        currencyCode: String?,
        uos: String?,
        vararg doConversions: Boolean
    ): String {
        val priceLower: Double
        val priceHigher: Double
        var currencySymbol = currencyCode
        val extendedCurrency = CurrencyCode.getCurrencySymbol(currencyCode)
        if (extendedCurrency != null) {
            currencySymbol = extendedCurrency.symbol
        }

        when (enumUnitOfSale) {
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

            EnumUnitOfSale.THOUSAND_KG -> {
                priceLower = (unitPriceLower * EnumUnitOfSale.THOUSAND_KG.unitWeight()) / 1000
                priceHigher = (unitPriceUpper * EnumUnitOfSale.THOUSAND_KG.unitWeight()) / 1000
            }

            else -> {
                priceLower = (unitPriceLower * EnumUnitOfSale.ONE_KG.unitWeight()) / 1000
                priceHigher = (unitPriceUpper * EnumUnitOfSale.ONE_KG.unitWeight()) / 1000
            }
        }

        minAmountUSD =
            priceLower // Minimum amount will be dynamic based on weight being sold, max amount will be constant

        return requireContext().getString(
            R.string.unit_price_label,
            priceLower,
            priceHigher,
            currencySymbol,
            uos
        )
    }
}
