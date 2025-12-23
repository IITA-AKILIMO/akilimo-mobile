package com.akilimo.mobile.ui.components

import android.view.View
import com.stepstone.stepper.StepperLayout
import com.stepstone.stepper.VerificationError

open class SimpleStepperListener : StepperLayout.StepperListener {
    override fun onCompleted(completeButton: View?) {}
    override fun onError(verificationError: VerificationError?) {}
    override fun onStepSelected(newStepPosition: Int) {}
    override fun onReturn() {}
}
