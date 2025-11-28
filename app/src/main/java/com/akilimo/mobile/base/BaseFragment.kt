package com.akilimo.mobile.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.akilimo.mobile.AppDatabase
import com.akilimo.mobile.helper.SessionManager
import timber.log.Timber


abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    protected lateinit var sessionManager: SessionManager
    protected lateinit var database: AppDatabase

    // ✅ Lifecycle-safe coroutine scope tied to view
    protected val safeScope get() = viewLifecycleOwner.lifecycleScope

    abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    /** Called after binding is safely initialized */
    protected abstract fun onBindingReady(savedInstanceState: Bundle?)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = inflateBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Safe initialization after view is created
        context?.let {
            sessionManager = SessionManager(it)
            database = AppDatabase.getDatabase(it)
        } ?: throw IllegalStateException("Context is not available for initialization")

        onBindingReady(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ✅ Utility: Show toast
    protected fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    // ✅ Utility: Log with fragment tag
    protected fun log(message: String) {
        Timber.tag(this::class.java.simpleName).d(message)
    }

    // ✅ Utility: Navigate with NavController
    protected fun navigate(@IdRes actionId: Int, args: Bundle? = null) {
        // findNavController().navigate(actionId, args)
    }

    // ✅ Optional: Access shared ViewModel
    protected inline fun <reified VM : ViewModel> activityViewModel(): VM {
        return ViewModelProvider(requireActivity())[VM::class.java]
    }
}