package com.akilimo.mobile.views.fragments.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.core.graphics.drawable.toDrawable
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.FragmentOperationTypeDialogBinding
import com.akilimo.mobile.inherit.BaseDialogFragment
import com.akilimo.mobile.interfaces.IDismissOperationsDialogListener
import com.akilimo.mobile.utils.enums.EnumOperation
import com.akilimo.mobile.utils.enums.EnumOperationMethod

class OperationTypeDialogFragment : BaseDialogFragment() {

    private var onDismissListener: IDismissOperationsDialogListener? = null
    private var selectedOperationType: EnumOperationMethod = EnumOperationMethod.NONE
    private var operationType: EnumOperation = EnumOperation.NONE
    private var wasCancelled = false

    private var _binding: FragmentOperationTypeDialogBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val TAG_OPERATION_DIALOG = "OperationTypeDialogFragment"
        const val OPERATION_TYPE = "operation_type_enum"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        operationType = arguments?.getParcelable(OPERATION_TYPE) ?: EnumOperation.NONE

        _binding = FragmentOperationTypeDialogBinding.inflate(layoutInflater)

        return Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.apply {
                setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                attributes.windowAnimations = R.style.DialogSlideAnimation
            }

            setContentView(binding.root)
            setCancelable(true)
            setCanceledOnTouchOutside(false)
            setupUI()
        }
    }

    private fun setupUI() {
        val titleRes = when (operationType) {
            EnumOperation.TILLAGE -> R.string.lbl_plough_op_type
            EnumOperation.RIDGING -> R.string.lbl_ridge_op_type
            else -> R.string.app_name
        }
        binding.lblFragmentTitle.setText(titleRes)

        binding.updateButton.setOnClickListener {
            if (selectedOperationType != EnumOperationMethod.NONE) {
                wasCancelled = false
                dismiss()
            } else {
                binding.lblError.visibility = View.VISIBLE
            }
        }

        binding.closeButton.setOnClickListener {
            wasCancelled = true
            selectedOperationType = EnumOperationMethod.NONE
            dismiss()
        }

        binding.rdgOperationType.setOnCheckedChangeListener { _, checkedId ->
            binding.lblError.visibility = View.GONE
            selectedOperationType = when (checkedId) {
                R.id.rdMechanical -> EnumOperationMethod.TRACTOR
                R.id.rdManual -> EnumOperationMethod.MANUAL
                else -> EnumOperationMethod.NONE
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.onDismiss(selectedOperationType, wasCancelled)
    }

    fun setOnDismissListener(listener: IDismissOperationsDialogListener?) {
        onDismissListener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
