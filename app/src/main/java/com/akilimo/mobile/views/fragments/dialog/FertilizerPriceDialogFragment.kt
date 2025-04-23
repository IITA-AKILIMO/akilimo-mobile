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
import com.akilimo.mobile.databinding.FragmentFertilizerPriceDialogBinding
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.entities.FertilizerPrice
import com.akilimo.mobile.inherit.BaseDialogFragment
import com.akilimo.mobile.interfaces.IFertilizerDismissListener
import com.akilimo.mobile.utils.CurrencyCode
import com.akilimo.mobile.utils.enums.EnumCountry
import io.sentry.Sentry

class FertilizerPriceDialogFragment : BaseDialogFragment() {
    private var isExactPriceRequired = false
    private var isPriceValid = false
    private var priceSpecified = false
    private var removeSelected = false

    private var dialog: Dialog? = null
    private var fertilizer: Fertilizer? = null
    private var fertilizerPricesList: List<FertilizerPrice>? = null

    private var savedPricePerBag = 0.0
    private var maxPrice = 0.0
    private var minPrice = 0.0
    private var countryCode: String? = null
    private var currencyCode: String? = null
    private var fertilizerKey: String? = null
    private var currencyName: String? = null
    private var bagPrice: Double? = null
    private var bagPriceRange: String? = "NA"
    private var exactPrice = 0.0

    private var onDismissListener: IFertilizerDismissListener? = null
    private var _binding: FragmentFertilizerPriceDialogBinding? = null
    private val binding get() = _binding!!

    companion object {
        private val LOG_TAG: String = FertilizerPriceDialogFragment::class.java.simpleName
        const val FERTILIZER_TYPE: String = "selected_type"
        const val ARG_ITEM_ID: String = "fertilizer_dialog_fragment"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bundle = arguments
        val context = requireContext()
        if (bundle != null) {
            fertilizer = bundle.getParcelable(FERTILIZER_TYPE)
        }
        dialog = Dialog(context)
        _binding = FragmentFertilizerPriceDialogBinding.inflate(layoutInflater)
        dialog!!.setContentView(binding.root)

        dialog!!.window!!.apply {
            requestFeature(Window.FEATURE_NO_TITLE)
            setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            )
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            attributes.windowAnimations = R.style.DialogSlideAnimation
        }

        if (fertilizer != null) {
            countryCode = fertilizer!!.countryCode
            currencyCode = fertilizer!!.currencyCode
            fertilizerKey = fertilizer!!.fertilizerKey
            currencySymbol = currencyCode
            val extendedCurrency = CurrencyCode.getCurrencySymbol(currencyCode)
            if (extendedCurrency != null) {
                currencySymbol = extendedCurrency.symbol
                currencyName = extendedCurrency.name
            }
            currencyName = when (countryCode) {
                "TZ" -> EnumCountry.Tanzania.currencyName(context)
                "NG" -> EnumCountry.Nigeria.currencyName(context)
                "GH" -> EnumCountry.Ghana.currencyName(context)
                "RW" -> EnumCountry.Rwanda.currencyName(context)
                "BI" -> EnumCountry.Burundi.currencyName(context)
                else -> currencyName
            }
            val titleText =
                context.getString(R.string.price_per_bag, fertilizer!!.name, currencyName)
            binding.lblFragmentTitle.text = titleText
        }

        binding.closeButton.setOnClickListener {
            priceSpecified = false
            removeSelected = false
            dismiss()
        }

        binding.removeButton.setOnClickListener {
            priceSpecified = false
            removeSelected = true
            fertilizer?.apply {
                price = 0.0
                pricePerBag = 0.0
                priceRange = null
                selected = false
                exactPrice = false
            }
            dismiss()
        }

        binding.updateButton.setOnClickListener {
            if (isExactPriceRequired) {
                try {
                    bagPrice = binding.editExactFertilizerPrice.text.toString().toDouble()
                } catch (ex: Exception) {
                    Sentry.captureException(ex)
                    bagPrice = 0.0
                }
                savedPricePerBag = bagPrice!!
                bagPriceRange = mathHelper!!.formatNumber(savedPricePerBag, currencyCode)
                isPriceValid = true
                binding.editExactFertilizerPrice.error = null
            }
            fertilizer?.apply {
                price = bagPrice!!
                pricePerBag = savedPricePerBag
                priceRange = bagPriceRange
                selected = true
                exactPrice = isExactPriceRequired
            }
            if (isPriceValid) {
                priceSpecified = true
                removeSelected = false
                dismiss()
            }
        }

        binding.radioGroup.setOnCheckedChangeListener { group, _ ->
            radioSelected(group)
        }

        if (database != null) {
            fertilizerPricesList =
                database!!.fertilizerPriceDao().findAllByFertilizerKey(fertilizerKey!!)
            addPriceRadioButtons(fertilizerPricesList!!, fertilizer)
        }

        return dialog!!
    }

    private fun radioSelected(radioGroup: RadioGroup) {
        val radioButtonId = radioGroup.checkedRadioButtonId
        val radioButton = dialog!!.findViewById<RadioButton>(radioButtonId)
        val itemTagIndex = radioButton.tag as Long

        currencySymbol = currencyCode
        CurrencyCode.getCurrencySymbol(currencyCode)?.let {
            currencySymbol = it.symbol
        }

        try {
            val pricesResp = database!!.fertilizerPriceDao().findOneByPriceId(itemTagIndex)
            isExactPriceRequired = false
            isPriceValid = true
            savedPricePerBag = pricesResp.pricePerBag
            bagPriceRange = pricesResp.priceRange
            bagPrice = savedPricePerBag
            binding.exactPriceWrapper.visibility = View.GONE
            binding.exactPriceWrapper.editText?.text = null

            if (savedPricePerBag == 0.0) {
                bagPrice = 0.0
                bagPriceRange = getString(R.string.lbl_unknown_price)
            } else if (savedPricePerBag < 0) {
                isExactPriceRequired = true
                isPriceValid = false
                binding.exactPriceWrapper.visibility = View.VISIBLE
            }
        } catch (ex: Exception) {
            Sentry.captureException(ex)
        }
    }

    private fun addPriceRadioButtons(
        fertilizerPricesList: List<FertilizerPrice>,
        fertilizer: Fertilizer?
    ) {
        binding.radioGroup.removeAllViews()
        val context = requireContext()
        var selectedPrice = 0.0
        currencySymbol = currencyCode
        CurrencyCode.getCurrencySymbol(currencyCode)?.let {
            currencySymbol = it.symbol
        }

        fertilizer?.let {
            selectedPrice = it.pricePerBag
            isExactPriceRequired = it.exactPrice
            if (it.selected) {
                binding.updateButton.setText(R.string.lbl_update)
                binding.removeButton.visibility = View.VISIBLE
            }
            if (isExactPriceRequired) {
                isPriceValid = true
                exactPrice = it.price
                if (exactPrice < 0) exactPrice = 0.0
                binding.editExactFertilizerPrice.setText(exactPrice.toString())
            }
        }

        for ((_, _, priceId, _, _, _, minAllowedPrice, maxAllowedPrice, price, _, priceRange) in fertilizerPricesList) {
            minPrice = minAllowedPrice
            maxPrice = maxAllowedPrice
            val listIndex = priceId.toLong()
            val radioButton = RadioButton(activity).apply {
                id = View.generateViewId()
                tag = listIndex
                text = when {
                    price == 0.0 -> context.getString(R.string.lbl_do_not_know)
                    price < 0 -> {
                        binding.exactPriceWrapper.hint =
                            getString(R.string.exact_fertilizer_price_currency, currencyName)
                        context.getString(R.string.exact_fertilizer_price)
                    }

                    else -> context.getString(R.string.lbl_about, priceRange)
                }
            }
            binding.radioGroup.addView(radioButton)

            if (isExactPriceRequired) {
                radioButton.isChecked = true
                isPriceValid = true
                binding.exactPriceWrapper.visibility = View.VISIBLE
                binding.editExactFertilizerPrice.setText(exactPrice.toString())
            } else if (price == selectedPrice) {
                radioButton.isChecked = true
            }
            isExactPriceRequired = false
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        _binding = null
        onDismissListener?.onDismiss(priceSpecified, fertilizer!!, removeSelected)
    }

    fun setOnDismissListener(dismissListener: IFertilizerDismissListener?) {
        this.onDismissListener = dismissListener
    }
}
