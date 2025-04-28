package com.akilimo.mobile.adapters

import android.content.Context
import android.os.Bundle
import androidx.annotation.IntRange
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.akilimo.mobile.R
import com.stepstone.stepper.Step
import com.stepstone.stepper.adapter.AbstractFragmentStepAdapter
import com.stepstone.stepper.viewmodel.StepViewModel

class MyStepperAdapter(
    supportFragmentManager: FragmentManager,
    private val ctx: Context,
    private val fragmentArray: List<Fragment>
) :
    AbstractFragmentStepAdapter(supportFragmentManager, ctx) {
    override fun createStep(position: Int): Step {
        val CURRENT_STEP_POSITION_KEY = "CURRENT_STEP_POSITION_KEY"
        val step = fragmentArray[position]
        val bundleParams = Bundle()
        bundleParams.putInt(CURRENT_STEP_POSITION_KEY, position)
        step.arguments = bundleParams

        return step as Step
    }

    override fun getCount(): Int {
        return fragmentArray.size
    }

    override fun getViewModel(@IntRange(from = 0) position: Int): StepViewModel {
        val builder = StepViewModel.Builder(ctx)
        if (position == 0) {
            builder.setBackButtonLabel(R.string.lbl_cancel)
        }
        return builder.create()
    }
}
