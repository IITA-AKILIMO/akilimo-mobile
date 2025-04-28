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
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.FragmentCassavaPriceDialogBinding
import com.akilimo.mobile.entities.MaizePrice
import com.akilimo.mobile.inherit.BaseDialogFragment
import com.akilimo.mobile.interfaces.IPriceDialogDismissListener
import com.akilimo.mobile.utils.CurrencyCode
import com.akilimo.mobile.utils.enums.EnumUnitOfSale
import io.sentry.Sentry


/**
 * A simple [androidx.fragment.app.Fragment] subclass.
 */
class MaizePriceDialogFragment : BaseDialogFragment() {
    private var isExactPriceRequired = false
    private var isPriceValid = false

    private var averagePrice = 0.0
    private var maizePrice = 0.0
    private var currencyName: String? = null
    private var maizePriceList: MutableList<MaizePrice>? = null

    private var countryCode: String? = null
    private var currencyCode: String? = null
    private var produceType: String? = null
    private var unitOfSale: String? = null
    private var unitOfSaleEnum: EnumUnitOfSale? = null

    private var onDismissListener: IPriceDialogDismissListener? = null

    private var _binding: FragmentCassavaPriceDialogBinding? = null
    private val binding get() = _binding!!

    companion object {
        private val LOG_TAG: String = MaizePriceDialogFragment::class.java.simpleName

        const val ARG_ITEM_ID: String = "mazie_price_dialog_fragment"
        const val AVERAGE_PRICE: String = "average_price"
        const val PRODUCE_TYPE: String = "produce_type"
        const val SELECTED_PRICE: String = "selected_price"
        const val UNIT_OF_SALE: String = "unit_of_sale"
        const val ENUM_UNIT_OF_SALE: String = "enum_unit_of_sale"
        const val CURRENCY_CODE: String = "currency_code"
        const val CURRENCY_NAME: String = "currency_name"
        const val COUNTRY_CODE: String = "country_code"
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bundle = this.arguments
        val context = requireContext()
        if (bundle != null) {
            averagePrice = bundle.getDouble(AVERAGE_PRICE)
            maizePrice = bundle.getDouble(SELECTED_PRICE)
            produceType = bundle.getString(PRODUCE_TYPE, "grain")
            currencyCode = bundle.getString(CURRENCY_CODE)
            currencyName = bundle.getString(CURRENCY_NAME)
            unitOfSale = bundle.getString(UNIT_OF_SALE)
            countryCode = bundle.getString(COUNTRY_CODE)
            unitOfSaleEnum = bundle.getParcelable(ENUM_UNIT_OF_SALE)
        }

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

        binding.closeButton.setOnClickListener(View.OnClickListener { view: View? ->
            dismiss()
        })

        binding.removeButton.setOnClickListener(View.OnClickListener { view: View? ->
            averagePrice = 0.0
            dismiss()
        })
        //save the data
        binding.updateButton.setOnClickListener(View.OnClickListener { v: View? ->
            if (isExactPriceRequired) {
                try {
                    maizePrice = binding.editExactFertilizerPrice.getText().toString().toDouble()
                } catch (ex: Exception) {
                    Sentry.captureException(ex)
                    maizePrice = 0.0
                }
                if (maizePrice <= 0) {
                    binding.editExactFertilizerPrice.error =
                        getString(R.string.lbl_provide_valid_unit_price)
                    isPriceValid = false
                    return@OnClickListener
                }
                isPriceValid = true
                binding.editExactFertilizerPrice.error = null
            }
            if (isPriceValid) {
                dismiss()
            }
        })

        binding.radioGroup.setOnCheckedChangeListener { radioGroup: RadioGroup, i: Int ->
            radioSelected(
                radioGroup, dialog
            )
        }
        if (database != null) {
            maizePriceList = database!!.maizePriceDao().findAllByCountryAndProduceType(
                countryCode!!,
                produceType!!
            )
            addPriceRadioButtons(maizePriceList!!, averagePrice)
        }
        return dialog
    }

    private fun radioSelected(radioGroup: RadioGroup, dialog: Dialog) {
        val radioButtonId = radioGroup.checkedRadioButtonId
        val radioButton = dialog.findViewById<RadioButton>(radioButtonId)
        val itemTagIndex = radioButton.tag as Long

        try {
            val pricesResp = database!!.maizePriceDao().findById(itemTagIndex.toInt())
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
                maizePrice = pricesResp.averagePrice
            }
        } catch (ex: Exception) {
            Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
            Sentry.captureException(ex)
        }
    }

    private fun addPriceRadioButtons(maizePriceList: MutableList<MaizePrice>, avgPrice: Double) {
        binding.radioGroup.removeAllViews()
        val selectedPrice = 0.0

        if (avgPrice < 0) {
            binding.updateButton.setText(R.string.lbl_update)
            binding.removeButton.visibility = View.VISIBLE
        }

        for (pricesResp in maizePriceList) {
            val price = pricesResp.averagePrice
            val listIndex: Long = pricesResp.id

            val radioButton = RadioButton(activity)
            radioButton.id = View.generateViewId()
            radioButton.tag = listIndex


            var radioLabel = labelText(
                pricesResp.minLocalPrice,
                pricesResp.maxLocalPrice,
                currencyCode,
                unitOfSale
            )
            if (price < 0) {
                radioLabel = requireContext().getString(R.string.lbl_exact_price_x_per_unit_of_sale)
                val exactTextHint =
                    getString(R.string.exact_fertilizer_price_currency, currencyName)
                binding.exactPriceWrapper.hint = exactTextHint
            } else if (price == 0.0) {
                radioLabel = requireContext().getString(R.string.lbl_do_not_know)
            }
            radioButton.text = radioLabel
            binding.radioGroup.addView(radioButton)

            //set relevant radio button as selected based on the price range
            if (avgPrice < 0) {
                radioButton.isChecked = true
                isPriceValid = true
                isExactPriceRequired = true
                binding.exactPriceWrapper.visibility = View.VISIBLE
                binding.editExactFertilizerPrice.setText(maizePrice.toString())
            }

            if (pricesResp.averagePrice == selectedPrice) {
                radioButton.isChecked = true
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (onDismissListener != null) {
            onDismissListener!!.onDismiss(maizePrice, isExactPriceRequired)
        }
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
        var priceLower = unitPriceLower
        var priceHigher = unitPriceUpper
        var currencySymbol = currencyCode
        val extendedCurrency = CurrencyCode.getCurrencySymbol(currencyCode)
        if (extendedCurrency != null) {
            currencySymbol = extendedCurrency.symbol
        }

        var finalPrice = 0.0
        when (unitOfSaleEnum) {
            EnumUnitOfSale.ONE_KG -> {
                priceLower = (unitPriceLower * EnumUnitOfSale.ONE_KG.unitWeight()) / 1000
                priceHigher = (unitPriceUpper * EnumUnitOfSale.ONE_KG.unitWeight()) / 1000

                finalPrice = mathHelper!!.roundToNearestSpecifiedValue(priceHigher, 10.0)
            }

            EnumUnitOfSale.FIFTY_KG -> {
                priceLower = (unitPriceLower * EnumUnitOfSale.FIFTY_KG.unitWeight()) / 1000
                priceHigher = (unitPriceUpper * EnumUnitOfSale.FIFTY_KG.unitWeight()) / 1000

                finalPrice = mathHelper!!.roundToNearestSpecifiedValue(priceHigher, 10.0)
            }

            EnumUnitOfSale.HUNDRED_KG -> {
                priceLower = (unitPriceLower * EnumUnitOfSale.HUNDRED_KG.unitWeight()) / 1000
                priceHigher = (unitPriceUpper * EnumUnitOfSale.HUNDRED_KG.unitWeight()) / 1000

                finalPrice = mathHelper!!.roundToNearestSpecifiedValue(priceHigher, 100.0)
            }

            EnumUnitOfSale.FRESH_COB -> finalPrice = priceHigher
            EnumUnitOfSale.NA -> TODO()
            EnumUnitOfSale.THOUSAND_KG -> TODO()
            null -> TODO()
        }
        return requireContext().getString(
            R.string.unit_price_label_single,
            finalPrice,
            currencySymbol,
            uos
        )
    }
}
