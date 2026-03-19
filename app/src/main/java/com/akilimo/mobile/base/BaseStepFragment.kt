package com.akilimo.mobile.base

import android.widget.Toast
import androidx.viewbinding.ViewBinding
import com.akilimo.mobile.wizard.ValidationError
import com.akilimo.mobile.wizard.WizardStep

abstract class BaseStepFragment<VB : ViewBinding> : BaseFragment<VB>(), WizardStep {

    override fun verifyStep(): ValidationError? = null

    protected open fun prefillFromEntity() {}

    override fun onSelected() {
        prefillFromEntity()
    }

    override fun onError(error: ValidationError) {
        Toast.makeText(requireContext(), error.errorMessage, Toast.LENGTH_SHORT).show()
    }
}
