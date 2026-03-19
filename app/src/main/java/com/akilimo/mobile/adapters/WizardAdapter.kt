package com.akilimo.mobile.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.akilimo.mobile.wizard.WizardStep

class WizardAdapter(
    activity: FragmentActivity,
    private val steps: List<WizardStep>
) : FragmentStateAdapter(activity) {

    override fun createFragment(position: Int): Fragment = steps[position] as Fragment

    override fun getItemCount(): Int = steps.size

    fun getStep(position: Int): WizardStep = steps[position]
}
