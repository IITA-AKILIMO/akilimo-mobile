package com.akilimo.mobile.views.fragments.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.FragmentOperationTypeDialogBinding
import com.akilimo.mobile.inherit.BaseDialogFragment
import com.akilimo.mobile.interfaces.IDismissOperationsDialogListener
import com.akilimo.mobile.utils.enums.EnumOperationType

class OperationTypeDialogFragment : BaseDialogFragment() {

    private var onDismissListener: IDismissOperationsDialogListener? = null
    private var enumOperationType: EnumOperationType? = null
    private var operation: String? = null
    private var cancelled = false

    private var _binding: FragmentOperationTypeDialogBinding? = null
    private val binding get() = _binding!!


    companion object {
        const val ARG_ITEM_ID: String = "OperationTypeDialogFragment"
        const val OPERATION_TYPE: String = "operation_type"
        private val LOG_TAG: String = OperationTypeDialogFragment::class.java.simpleName
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bundle = this.arguments
        val context = requireContext()
        if (bundle != null) {
            operation = bundle.getString(OPERATION_TYPE)
        }

        _binding = FragmentOperationTypeDialogBinding.inflate(layoutInflater)
        val dialog = Dialog(context)

        dialog.setContentView(binding.root)

        dialog.window!!.apply {
            requestFeature(Window.FEATURE_NO_TITLE)
            setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            )
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            attributes.windowAnimations = R.style.DialogSlideAnimation
            isCancelable = true
        }
        dialog.setCanceledOnTouchOutside(false)


        val lblFragmentTitle = dialog.findViewById<TextView>(R.id.lblFragmentTitle)
        if (operation == "Plough") {
            lblFragmentTitle.setText(R.string.lbl_plough_op_type)
        } else if (operation == "Ridge") {
            lblFragmentTitle.setText(R.string.lbl_ridge_op_type)
        }

        binding.updateButton.setOnClickListener(View.OnClickListener { view: View? ->
            if (enumOperationType != null) {
                cancelled = false
                dismiss()
            }
            binding.lblError.visibility = View.VISIBLE
        })

        binding.closeButton.setOnClickListener(View.OnClickListener { view: View? ->
            cancelled = true
            enumOperationType = EnumOperationType.NONE
            dismiss()
        })

        binding.rdgOperationType.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup: RadioGroup?, radioIndex: Int ->
            radioSelected(
                radioIndex
            )
        })
        return dialog
    }

    private fun radioSelected(radioIndex: Int) {
        binding.lblError.visibility = View.GONE
        enumOperationType = when (radioIndex) {
            R.id.rdMechanical -> EnumOperationType.MECHANICAL
            R.id.rdManual -> EnumOperationType.MANUAL
            else -> EnumOperationType.NONE
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (onDismissListener != null && enumOperationType != null) {
            onDismissListener!!.onDismiss(operation!!, enumOperationType!!, cancelled)
        }
    }

    fun setOnDismissListener(dismissListener: IDismissOperationsDialogListener?) {
        this.onDismissListener = dismissListener
    }
}
