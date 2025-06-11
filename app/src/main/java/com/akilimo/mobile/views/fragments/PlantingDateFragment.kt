package com.akilimo.mobile.views.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.FragmentPlantingHarvestDateBinding
import com.akilimo.mobile.entities.CropSchedule
import com.akilimo.mobile.inherit.BindBaseStepFragment
import com.akilimo.mobile.utils.DateHelper.olderThanCurrent
import com.akilimo.mobile.views.fragments.dialog.DateDialogPickerFragment
import com.stepstone.stepper.VerificationError
import io.sentry.Sentry


/**
 * A simple [androidx.fragment.app.Fragment] subclass.
 */
class PlantingDateFragment : BindBaseStepFragment<FragmentPlantingHarvestDateBinding>() {


    private var selectedPlantingDate: String = ""
    private var selectedHarvestDate: String = ""
    private var alreadyPlanted = false

    companion object {
        fun newInstance(): PlantingDateFragment {
            return PlantingDateFragment()
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentPlantingHarvestDateBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {

        binding.plantingBtnPickDate.setOnClickListener { v: View? ->
            // create the datePickerFragment
            val newFragment = DateDialogPickerFragment.newInstanceForPlanting()
            // set the targetFragment to receive the results, specifying the request code
            newFragment.setTargetFragment(
                this@PlantingDateFragment,
                DateDialogPickerFragment.PLANTING_REQUEST_CODE
            )
            // show the datePicker
            newFragment.show(parentFragmentManager, "PlantingDatePicker")
        }

        binding.harvestBtnPickDate.setOnClickListener { v: View? ->
            // create the datePickerFragment
            val newFragment = DateDialogPickerFragment.newInstanceForHarvest(selectedPlantingDate)
            // set the targetFragment to receive the results, specifying the request code
            newFragment.setTargetFragment(
                this@PlantingDateFragment,
                DateDialogPickerFragment.HARVEST_REQUEST_CODE
            )
            // show the datePicker
            newFragment.show(parentFragmentManager, "HarvestDatePicker")
        }
    }

    private fun refreshData() {
        try {
            val cropSchedule = database.scheduleDateDao().findOne()
            if (cropSchedule != null) {
                selectedPlantingDate = cropSchedule.plantingDate
                selectedHarvestDate = cropSchedule.harvestDate
                binding.plantingDateLabel.text = selectedPlantingDate
                binding.harvestDateLabel.text = selectedHarvestDate
            }
        } catch (ex: Exception) {
            Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
            Sentry.captureException(ex)
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // check for the results
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == DateDialogPickerFragment.PLANTING_REQUEST_CODE) {
                selectedPlantingDate = data.getStringExtra("selectedDate") ?: ""
                selectedHarvestDate = ""
            } else if (requestCode == DateDialogPickerFragment.HARVEST_REQUEST_CODE) {
                selectedHarvestDate = data.getStringExtra("selectedDate") ?: ""
            }
        }
        binding.plantingDateLabel.text = selectedPlantingDate
        binding.harvestDateLabel.text = selectedHarvestDate
    }

    private fun saveEntities() {
        dataIsValid = selectedPlantingDate.isNotEmpty()
        if (!dataIsValid) {
            errorMessage = requireContext().getString(R.string.lbl_planting_date_prompt)
            return
        }
        dataIsValid = selectedHarvestDate.isNotEmpty()
        if (!dataIsValid) {
            errorMessage = requireContext().getString(R.string.lbl_harvest_date_prompt)
            return
        }
        try {
            alreadyPlanted = olderThanCurrent(selectedPlantingDate)

            var cropSchedule = database.scheduleDateDao().findOne()
            if (cropSchedule == null) {
                cropSchedule = CropSchedule()
            }
            cropSchedule.plantingDate = selectedPlantingDate
            cropSchedule.harvestDate = selectedHarvestDate
            cropSchedule.alreadyPlanted = alreadyPlanted
            database.scheduleDateDao().insert(cropSchedule)
            dataIsValid = true
        } catch (ex: Exception) {
            dataIsValid = false
            errorMessage = ex.message!!
            Sentry.captureException(ex)
        }
    }

    override fun verifyStep(): VerificationError? {
        saveEntities()
        if (!dataIsValid) {
            showCustomWarningDialog(errorMessage)
            return VerificationError(errorMessage)
        }
        return null
    }

    override fun onSelected() {
        refreshData()
    }
}
