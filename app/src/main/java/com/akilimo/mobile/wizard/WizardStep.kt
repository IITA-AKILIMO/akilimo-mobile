package com.akilimo.mobile.wizard

interface WizardStep {
    /** Return null if valid, or a [ValidationError] to block navigation. */
    fun verifyStep(): ValidationError?

    /** Called when this step becomes visible. */
    fun onSelected() {}

    /** Called when [verifyStep] returned an error and the user tried to proceed. */
    fun onError(error: ValidationError) {}
}
