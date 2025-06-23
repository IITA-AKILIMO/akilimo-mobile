package com.akilimo.mobile.inherit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.stepstone.stepper.VerificationError

abstract class BindBaseStepFragment<T : ViewBinding> : BaseStepFragment() {
    private var _binding: T? = null
    protected val binding: T
        get() = _binding ?: error(
            "ViewBinding accessed before onCreateView() or after onDestroyView()."
        )

    protected abstract fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): T

    /**
     * Subclasses must implement this. Called after binding is safely initialized.
     */
    protected abstract fun onBindingReady(savedInstanceState: Bundle?)

    abstract fun setupObservers()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = inflateBinding(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onBindingReady(savedInstanceState)
    }

    override fun verifyStep(): VerificationError? = null

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
