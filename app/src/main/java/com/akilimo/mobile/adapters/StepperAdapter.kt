package com.akilimo.mobile.adapters

import android.content.Context
import androidx.annotation.IntRange
import androidx.fragment.app.FragmentManager
import com.stepstone.stepper.Step
import com.stepstone.stepper.adapter.AbstractFragmentStepAdapter
import com.stepstone.stepper.viewmodel.StepViewModel

class StepperAdapter(
    fm: FragmentManager,
    context: Context,
    private val steps: List<Step>
) : AbstractFragmentStepAdapter(fm, context) {

    override fun createStep(position: Int): Step = steps[position]

    override fun getCount(): Int = steps.size

    override fun getViewModel(@IntRange(from = 0) position: Int): StepViewModel =
        StepViewModel.Builder(context)
            .setTitle("Step ${position + 1}")
            .create()
}

