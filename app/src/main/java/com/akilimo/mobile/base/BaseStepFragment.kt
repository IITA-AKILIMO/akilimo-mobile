package com.akilimo.mobile.base

import android.widget.Toast
import androidx.viewbinding.ViewBinding
import com.stepstone.stepper.Step
import com.stepstone.stepper.VerificationError

abstract class BaseStepFragment<VB : ViewBinding> : BaseFragment<VB>(), Step {

    override fun verifyStep(): VerificationError? = null
    protected open fun prefillFromEntity() {
        //to be overriden
    }

    override fun onSelected() {
        prefillFromEntity()
    }

    // Called when validation fails
    override fun onError(error: VerificationError) {
        Toast.makeText(requireContext(), error.errorMessage, Toast.LENGTH_SHORT).show()
    }
}