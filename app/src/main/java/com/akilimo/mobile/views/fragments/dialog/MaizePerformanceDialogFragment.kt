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
import com.akilimo.mobile.databinding.FragmentMaizePerfDialogBinding
import com.akilimo.mobile.entities.MaizePerformance
import com.akilimo.mobile.inherit.BaseDialogFragment
import com.akilimo.mobile.interfaces.IMaizePerformanceDismissListener
import com.akilimo.mobile.utils.Tools.displayImageOriginal


/**
 * A simple [androidx.fragment.app.Fragment] subclass.
 */
class MaizePerformanceDialogFragment : BaseDialogFragment() {
    private var performanceConfirmed = false


    private var maizePerformance: MaizePerformance? = null

    private var onDismissListener: IMaizePerformanceDismissListener? = null
    private var _binding: FragmentMaizePerfDialogBinding? = null

    private val binding get() = _binding!!

    companion object {
        const val PERFORMANCE_DATA: String = "performance_data"
        const val ARG_ITEM_ID: String = "maize_performance_dialog_fragment"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bundle = this.arguments
        val context = requireContext()
        if (bundle != null) {
            maizePerformance = bundle.getParcelable(PERFORMANCE_DATA)
        }
        _binding = FragmentMaizePerfDialogBinding.inflate(layoutInflater)


        val dialog = Dialog(context)
        dialog.apply {
            window!!.requestFeature(Window.FEATURE_NO_TITLE)
            window!!.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            )
            window!!.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            window!!.attributes.windowAnimations = R.style.DialogSlideAnimation

            setContentView(binding.root)
        }

        if (maizePerformance != null) {
            val yieldAmountLabel = maizePerformance!!.maizePerformanceLabel
            val yieldDesc = maizePerformance!!.maizePerformanceDesc

            binding.lblFragmentTitle.text = yieldAmountLabel
            binding.perfDescription.text = yieldDesc

            displayImageOriginal(this.context, binding.rootYieldImage, maizePerformance!!.imageId)
        }


        binding.closeButton.setOnClickListener { view: View? ->
            performanceConfirmed = false
            dismiss()
        }

        binding.cancelButton.setOnClickListener { view: View? ->
            performanceConfirmed = false
            dismiss()
        }

        binding.confirmButton.setOnClickListener { v: View? ->
            performanceConfirmed = true
            dismiss()
        }

        return dialog
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (onDismissListener != null) {
            onDismissListener!!.onDismiss(maizePerformance!!, performanceConfirmed)
        }
    }

    fun setOnDismissListener(dismissListener: IMaizePerformanceDismissListener?) {
        this.onDismissListener = dismissListener
    }


}
