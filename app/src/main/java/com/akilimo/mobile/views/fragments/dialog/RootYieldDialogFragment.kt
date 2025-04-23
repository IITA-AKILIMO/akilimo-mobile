package com.akilimo.mobile.views.fragments.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.graphics.drawable.toDrawable
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.FragmentRootYieldDialogBinding
import com.akilimo.mobile.entities.FieldYield
import com.akilimo.mobile.inherit.BaseDialogFragment
import com.akilimo.mobile.interfaces.IFieldYieldDismissListener
import com.akilimo.mobile.utils.Tools.displayImageOriginal


/**
 * A simple [androidx.fragment.app.Fragment] subclass.
 */
class RootYieldDialogFragment : BaseDialogFragment() {
    private var yieldConfirmed = false

    private var fieldYield: FieldYield? = null

    private var onDismissListener: IFieldYieldDismissListener? = null

    private var _binding: FragmentRootYieldDialogBinding? = null
    private val binding get() = _binding!!

    companion object {
        private val LOG_TAG: String = RootYieldDialogFragment::class.java.simpleName

        const val YIELD_DATA: String = "yield_data"
        const val ARG_ITEM_ID: String = "root_yield_dialog_fragment"
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bundle = this.arguments
        val context = requireContext()
        if (bundle != null) {
            fieldYield = bundle.getParcelable(YIELD_DATA)
        }
        val dialog = Dialog(context)

        _binding = FragmentRootYieldDialogBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        dialog.window!!.apply {
            requestFeature(Window.FEATURE_NO_TITLE)
            setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            )
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            attributes.windowAnimations = R.style.DialogSlideAnimation
        }

        dialog.setCancelable(true)

        if (fieldYield != null) {
            val yieldLabel = fieldYield!!.fieldYieldLabel
            val yieldAmountLabel = fieldYield!!.fieldYieldAmountLabel
            val yieldDesc = fieldYield!!.fieldYieldDesc
            val selectedTitle =
                getString(R.string.lbl_you_expect_yield, yieldAmountLabel!!.lowercase(), yieldDesc)

            displayImageOriginal(this.context, binding.rootYieldImage, fieldYield!!.imageId)
            binding.lblFragmentTitle.text = selectedTitle
        }


        binding.closeButton.setOnClickListener(View.OnClickListener { view: View? ->
            yieldConfirmed = false
            dismiss()
        })

        binding.cancelButton.setOnClickListener(View.OnClickListener { view: View? ->
            yieldConfirmed = false
            dismiss()
        })
        //save the data
        binding.confirmButton.setOnClickListener(View.OnClickListener { v: View? ->
            yieldConfirmed = true
            dismiss()
        })

        return dialog
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (onDismissListener != null) {
            onDismissListener!!.onDismiss(fieldYield!!, yieldConfirmed)
        }
    }

    fun setOnDismissListener(dismissListener: IFieldYieldDismissListener?) {
        this.onDismissListener = dismissListener
    }
}
