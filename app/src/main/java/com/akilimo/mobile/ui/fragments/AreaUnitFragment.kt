package com.akilimo.mobile.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.ValueOptionAdapter
import com.akilimo.mobile.base.BaseStepFragment
import com.akilimo.mobile.databinding.FragmentAreaUnitBinding
import com.akilimo.mobile.dto.AreaUnitOption
import com.akilimo.mobile.dto.FieldSizeOption
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.enums.EnumAreaUnit
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.enums.EnumFieldArea
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.utils.MathHelper
import com.stepstone.stepper.VerificationError
import kotlinx.coroutines.launch

class AreaUnitFragment : BaseStepFragment<FragmentAreaUnitBinding>() {

    companion object {
        fun newInstance() = AreaUnitFragment()
    }

    private lateinit var userRepo: AkilimoUserRepo
    val areaUnitOptions = mutableListOf<AreaUnitOption>()
    private lateinit var baseFieldSizes: List<FieldSizeOption>


    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentAreaUnitBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        userRepo = AkilimoUserRepo(database.akilimoUserDao())
        baseFieldSizes = listOf(
            FieldSizeOption(
                getString(R.string.quarter_acre),
                EnumFieldArea.QUARTER_ACRE.areaValue(),
            ),
            FieldSizeOption(
                getString(R.string.half_acre),
                EnumFieldArea.HALF_ACRE.areaValue()
            ),
            FieldSizeOption(
                getString(R.string.one_acre),
                EnumFieldArea.ONE_ACRE.areaValue()
            ),
            FieldSizeOption(
                getString(R.string.one_half_acre),
                EnumFieldArea.ONE_HALF_ACRE.areaValue()
            ),
            FieldSizeOption(
                getString(R.string.two_half_acres),
                EnumFieldArea.TWO_HALF_ACRE.areaValue()
            ),
            FieldSizeOption(
                getString(R.string.exact_field_area),
                EnumFieldArea.EXACT_AREA.areaValue()
            )
        )

        areaUnitOptions.add(
            AreaUnitOption(
                getString(R.string.area_unit_acre),
                EnumAreaUnit.ACRE
            )
        )
        areaUnitOptions.add(
            AreaUnitOption(
                getString(R.string.area_unit_ha),
                EnumAreaUnit.HA
            )
        )
        areaUnitOptions.add(
            AreaUnitOption(
                getString(R.string.area_unit_square_meter),
                EnumAreaUnit.M2
            )
        )
        safeScope.launch {
            val user = userRepo.getUser(sessionManager.akilimoUser) ?: return@launch
            val countryCode = user.enumCountry
            if (countryCode == EnumCountry.RW) {
                areaUnitOptions.add(
                    AreaUnitOption(
                        getString(R.string.area_unit_are),
                        EnumAreaUnit.ARE
                    )
                )
            }

            val areaUnitAdapter = ValueOptionAdapter(requireContext(), areaUnitOptions)
            binding.dropAreaUnit.setAdapter(areaUnitAdapter)

            binding.dropAreaUnit.setOnItemClickListener { _, _, position, _ ->
                val selected = areaUnitAdapter.getItem(position) ?: return@setOnItemClickListener
                binding.dropAreaUnit.setText(selected.displayLabel, false)
                populateFieldSizeDropdown(selected.valueOption)
                areaUnitAdapter.setSelectedValue(selected.valueOption)
                binding.lytCustomFieldSize.hint =
                    getString(
                        R.string.lbl_cassava_field_size,
                        selected.valueOption.label(requireContext())
                    )
            }

            binding.lytRememberArea.chkRememberDetails.setOnCheckedChangeListener { _, isChecked ->
                sessionManager.rememberAreaUnit = isChecked
            }
        }
    }

    override fun prefillFromEntity() {
        safeScope.launch {
            val user = userRepo.getUser(sessionManager.akilimoUser)
            if (user == null) {
                populateFieldSizeDropdown(EnumAreaUnit.ACRE)
                return@launch
            }

            // Set area unit label
            areaUnitOptions.find { it.valueOption == user.enumAreaUnit }?.let { option ->
                populateFieldSizeDropdown(option.valueOption)
                binding.dropAreaUnit.setText(option.displayLabel, false)
                binding.lytCustomFieldSize.hint =
                    getString(
                        R.string.lbl_cassava_field_size,
                        option.valueOption.label(requireContext())
                    )
            }
            val areaUnit = user.enumAreaUnit

            val unit =
                EnumAreaUnit.entries.firstOrNull {
                    it.name.equals(
                        areaUnit.name,
                        ignoreCase = true
                    )
                }


            val options = convertFieldSizes(unit)

            val label =
                options.find { it.valueOption == user.farmSize }?.displayLabel
                    ?: getString(R.string.exact_field_area)

            binding.dropFieldSizeOptions.setText(label, false)

            // Toggle custom field size visibility
            binding.lytCustomFieldSize.visibility = View.GONE
            if (user.customFarmSize == true) {
                binding.lytCustomFieldSize.visibility = View.VISIBLE
                binding.inputCustomFieldSize.setText(user.farmSize.toString())
            }
        }
    }

    private fun populateFieldSizeDropdown(unit: EnumAreaUnit) {
        val options = convertFieldSizes(unit)
        val adapter = ValueOptionAdapter(requireContext(), options)
        binding.dropFieldSizeOptions.setAdapter(adapter)

        binding.dropFieldSizeOptions.setOnItemClickListener { _, _, position, _ ->
            val selected = adapter.getItem(position) ?: return@setOnItemClickListener

            adapter.setSelectedValue(selected.valueOption)
            binding.dropFieldSizeOptions.setText(selected.displayLabel, false)
            if (selected.valueOption <= 0.0) {
                binding.lytCustomFieldSize.visibility = View.VISIBLE
                binding.inputCustomFieldSize.text?.clear()
            } else {
                binding.lytCustomFieldSize.visibility = View.GONE
                binding.inputCustomFieldSize.setText(selected.valueOption.toString())
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun convertFieldSizes(unit: EnumAreaUnit?): List<FieldSizeOption> {
        if (unit == null) return emptyList()
        return baseFieldSizes.map { base ->
            if (base.valueOption == EnumFieldArea.EXACT_AREA.areaValue()) {
                base.copy(
                    displayLabel = getString(R.string.exact_field_area),
                    valueOption = EnumFieldArea.EXACT_AREA.areaValue()
                )
            } else {
                val converted = MathHelper.convertFromAcres(base.valueOption, unit)
                val label = String.format("%,.2f %s", converted, unit.label(requireContext()))
                base.copy(displayLabel = label, valueOption = converted)
            }
        }
    }

    override fun verifyStep(): VerificationError? = with(binding) {
        val selectedUnitLabel = dropAreaUnit.text.toString()
        val enumAreaUnits =
            areaUnitOptions.find { it.displayLabel == selectedUnitLabel }?.valueOption

        val options = convertFieldSizes(enumAreaUnits)
        val selectedSizeLabel = dropFieldSizeOptions.text.toString()
        val selectedFieldSize =
            options.find { it.displayLabel == selectedSizeLabel }?.valueOption ?: 0.0

        val fieldSize = when (selectedFieldSize) {
            EnumFieldArea.EXACT_AREA.areaValue() -> inputCustomFieldSize.text?.toString()
                ?.toDoubleOrNull() ?: 0.0

            else -> selectedFieldSize
        }

        val exactFieldSize = selectedFieldSize == EnumFieldArea.EXACT_AREA.areaValue()
        lytAreaUnit.error = null
        lytFieldSizeOptions.error = null
        lytCustomFieldSize.error = null

        if (enumAreaUnits?.name.isNullOrEmpty()) {
            val message = getString(R.string.lbl_area_unit_prompt)
            lytAreaUnit.error = message
            return VerificationError(message)
        }

        if (fieldSize <= 0.0) {
            val message =
                getString(R.string.lbl_field_size_prompt, enumAreaUnits.label(requireContext()))
            if (exactFieldSize) {
                lytCustomFieldSize.error = message
            } else {
                lytFieldSizeOptions.error = message
            }
            return VerificationError(message)
        }


        safeScope.launch {
            val user = userRepo.getUser(sessionManager.akilimoUser) ?: AkilimoUser(
                userName = sessionManager.akilimoUser
            )
            userRepo.saveOrUpdateUser(
                user.copy(
                    enumAreaUnit = enumAreaUnits,
                    farmSize = fieldSize,
                    customFarmSize = exactFieldSize
                ),
                sessionManager.akilimoUser
            )
        }

        return null
    }
}