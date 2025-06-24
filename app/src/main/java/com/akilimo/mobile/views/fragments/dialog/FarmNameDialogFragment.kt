package com.akilimo.mobile.views.fragments.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.DialogFarmNameBinding
import com.akilimo.mobile.inherit.BaseBottomSheetDialogFragment

class FarmNameDialogFragment(
    private val onFarmNameEntered: (String) -> Unit
) : BaseBottomSheetDialogFragment() {

    private var _binding: DialogFarmNameBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogFarmNameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnSave.setOnClickListener {
            val name = binding.etFarmName.text?.toString()?.trim().orEmpty()
            if (name.isBlank()) {
                binding.lytFarmName.error = getString(R.string.error_farm_name_required)
            } else {
                binding.lytFarmName.error = null
                onFarmNameEntered(name)
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