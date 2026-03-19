package com.akilimo.mobile.base

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.viewbinding.ViewBinding
import com.akilimo.mobile.wizard.ValidationError
import com.akilimo.mobile.wizard.WizardStep

abstract class BaseStepFragment<VB : ViewBinding> : BaseFragment<VB>(), WizardStep {

    // ViewPager2 may call onSelected() before onCreateView() completes.
    // Track it and replay in onViewCreated() when the view is ready.
    private var pendingOnSelected = false

    override fun verifyStep(): ValidationError? = null

    protected open fun prefillFromEntity() {}

    override fun onSelected() {
        if (view != null) {
            prefillFromEntity()
        } else {
            pendingOnSelected = true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (pendingOnSelected) {
            pendingOnSelected = false
            prefillFromEntity()
        }
    }

    override fun onError(error: ValidationError) {
        Toast.makeText(requireContext(), error.errorMessage, Toast.LENGTH_SHORT).show()
    }
}
