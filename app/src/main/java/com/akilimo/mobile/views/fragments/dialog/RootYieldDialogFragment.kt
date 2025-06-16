package com.akilimo.mobile.views.fragments.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.FragmentRootYieldDialogBinding
import com.akilimo.mobile.entities.FieldYield
import com.akilimo.mobile.inherit.BaseBottomSheetDialogFragment
import com.akilimo.mobile.utils.Tools.displayImageOriginal

class RootYieldDialogFragment : BaseBottomSheetDialogFragment() {

    private var fieldYield: FieldYield? = null
    private var yieldConfirmed = false

    private var onConfirmClick: ((FieldYield) -> Unit)? = null
    private var onCancelClick: (() -> Unit)? = null

    private var _binding: FragmentRootYieldDialogBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val FIELD_YIELD_DATA = "yield_data"

        fun newInstance(
            selectedFieldYield: FieldYield,
            onConfirmClick: (FieldYield) -> Unit,
            onCancelClick: (() -> Unit)? = null,
        ): RootYieldDialogFragment {
            return RootYieldDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(FIELD_YIELD_DATA, selectedFieldYield)
                }
                this.onConfirmClick = onConfirmClick
                this.onCancelClick = onCancelClick
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            fieldYield = it.getParcelable(FIELD_YIELD_DATA)
        }
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRootYieldDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fieldYield?.let { fy ->
            val selectedTitle = getString(
                R.string.lbl_you_expect_yield,
                fy.fieldYieldAmountLabel?.lowercase(),
                fy.fieldYieldDesc
            )
            binding.tvRootYieldTitle.text = selectedTitle
            displayImageOriginal(context, binding.imgRootYield, fy.imageId)

            binding.btnCancelYield.setOnClickListener {
                onCancelClick?.invoke()
                dismissWithConfirmation(false)
            }

            binding.btnConfirmYield.setOnClickListener {
                onConfirmClick?.invoke(fy)
                dismissWithConfirmation(true)
            }
        }
    }

    private fun dismissWithConfirmation(confirmed: Boolean) {
        yieldConfirmed = confirmed
        dismiss()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
