package com.akilimo.mobile.views.fragments.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.akilimo.mobile.data.CountryOption
import com.akilimo.mobile.databinding.FragmentCountryPickerDialogBinding
import com.akilimo.mobile.inherit.BaseBottomSheetDialogFragment

class CountryPickerDialogFragment(
    private val countries: List<CountryOption>,
    private val selectedIndex: Int,
    private val onCountrySelected: (selectedIndex: Int) -> Unit
) : BaseBottomSheetDialogFragment() {


    private var _binding: FragmentCountryPickerDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCountryPickerDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val countryLabels = countries.map { it.displayLabel }

        val countryAdapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_single_choice,
            countryLabels
        )

        binding.listViewCountries.apply {
            adapter = countryAdapter
            choiceMode = android.widget.ListView.CHOICE_MODE_SINGLE
            setItemChecked(selectedIndex, true)
        }

        binding.btnSelect.setOnClickListener {
            val selected = binding.listViewCountries.checkedItemPosition
            if (selected >= 0) {
                onCountrySelected(selected)
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