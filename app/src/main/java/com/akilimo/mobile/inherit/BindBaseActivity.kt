package com.akilimo.mobile.inherit

import android.os.Bundle
import androidx.viewbinding.ViewBinding

abstract class BindBaseActivity<T : ViewBinding> : BaseActivity() {

    private var _binding: T? = null
    protected val binding
        get() = _binding ?: throw IllegalStateException("Binding is not initialized yet.")


    protected abstract fun inflateBinding(): T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = inflateBinding()
        setContentView(binding.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}