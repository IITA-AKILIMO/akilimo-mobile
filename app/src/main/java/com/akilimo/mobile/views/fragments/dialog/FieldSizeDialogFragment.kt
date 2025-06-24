package com.akilimo.mobile.views.fragments.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.DialogFieldSizeBinding
import com.akilimo.mobile.inherit.BaseBottomSheetDialogFragment

class FieldSizeDialogFragment(
    private val onFieldSizeEntered: (Double?) -> Unit
) : BaseBottomSheetDialogFragment() {

    private var _binding: DialogFieldSizeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogFieldSizeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnSave.setOnClickListener {
            val name = binding.etFieldSize.text?.toString()?.trim().orEmpty()
            if (name.isBlank()) {
                binding.lytFieldSize.error = getString(R.string.lbl_field_size_prompt)
            } else {
                binding.lytFieldSize.error = null
                val size = name.toDoubleOrNull()
                onFieldSizeEntered(size)
                dismiss()
            }
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}