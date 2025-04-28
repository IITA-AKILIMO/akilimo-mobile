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
import com.akilimo.mobile.databinding.DialogSingleItemBinding
import com.akilimo.mobile.inherit.BaseDialogFragment


/**
 * A simple [androidx.fragment.app.Fragment] subclass.
 */
class SingleSelectDialogFragment : BaseDialogFragment() {

    private var cancelled = false


    private var selectedIndex = -1
    private var radioButtonValueInt = 0
    private var selectedLabel: String? = null

    private var onDismissListener: IDismissDialog? = null
    private var risks: Array<String>? = null

    private var _binding: DialogSingleItemBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val RISK_LIST: String = "risk-list"
        const val RISK_INDEX: String = "risk-index"
        const val ARG_ITEM_ID: String = "single-select-dialog"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bundle = this.arguments
        val context = requireContext()
        if (bundle != null) {
            risks = bundle.getStringArray(RISK_LIST)
            selectedIndex = bundle.getInt(RISK_INDEX, -1)
        }

        val dialog = Dialog(context)
        _binding = DialogSingleItemBinding.inflate(layoutInflater)

        dialog.apply {
            window!!.requestFeature(Window.FEATURE_NO_TITLE)
            window!!.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            )
            window!!.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            window!!.attributes.windowAnimations = R.style.DialogSlideAnimation

            setContentView(binding.root)
            setCancelable(true)
        }



        binding.btnCancel.setOnClickListener { view: View? ->
            cancelled = true
            dismiss()
        }

        //save the data
        binding.btnSave.setOnClickListener { v: View? ->
            cancelled = false
            dismiss()
        }

        binding.rdgSingleChoice.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup: RadioGroup, i: Int ->
            radioSelected(
                radioGroup,
                dialog
            )
        })
        addCostRadioButtons(risks!!)
        return dialog
    }

    private fun radioSelected(radioGroup: RadioGroup, dialog: Dialog) {
        val radioButtonId = radioGroup.checkedRadioButtonId
        val radioButton = dialog.findViewById<RadioButton>(radioButtonId)
        val itemTagIndex = radioButton.tag as Int

        selectedLabel = risks!![itemTagIndex]
        radioButtonValueInt = when (itemTagIndex) {
            0 -> 0
            1 -> 1
            2 -> 2
            else -> 0
        }
    }

    private fun addCostRadioButtons(risks: Array<String>) {
        binding.rdgSingleChoice.removeAllViews()
        for (listIndex in risks.indices) {
            val radioButton = RadioButton(activity)
            radioButton.id = View.generateViewId()
            radioButton.tag = listIndex
            val radioLabel = risks[listIndex]

            selectedLabel = radioLabel
            radioButton.text = radioLabel
            binding.rdgSingleChoice.addView(radioButton)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (onDismissListener != null) {
            onDismissListener!!.onDismiss(
                selectedLabel,
                radioButtonValueInt,
                selectedIndex,
                cancelled
            )
        }
    }

    fun setOnDismissListener(dismissListener: IDismissDialog?) {
        this.onDismissListener = dismissListener
    }

    /* callback interface for pricing specification*/
    interface IDismissDialog {
        fun onDismiss(
            selectedLabel: String?,
            radioButtonValueInt: Int,
            selectedIndex: Int,
            cancelled: Boolean
        )
    }
}
