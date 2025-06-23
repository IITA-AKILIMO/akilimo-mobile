package com.akilimo.mobile.inherit

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.akilimo.mobile.R
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.utils.MathHelper
import io.sentry.Sentry

abstract class BaseFragment<T : ViewBinding> : Fragment() {

    private var _binding: T? = null
    protected val binding
        get() = _binding ?: error("Binding is not initialized yet.")


    protected var currencySymbol: String? = null
    protected var countryCode: String? = null

    protected val mathHelper: MathHelper by lazy { MathHelper() }
    protected val database: AppDatabase by lazy { AppDatabase.getInstance(requireContext().applicationContext) }

    protected abstract fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): T


    /**
     * Subclasses must implement this. Called after binding is safely initialized.
     */
    protected abstract fun onBindingReady(savedInstanceState: Bundle?)


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflateBinding(inflater, container, savedInstanceState)
        return binding.root
    }

    protected fun showCustomWarningDialog(
        titleText: String?, contentText: String?, buttonTitle: String? = null
    ) {
        try {
            val dialog = Dialog(requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
            dialog.setContentView(R.layout.dialog_warning)
            dialog.setCancelable(true)

            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog.window!!.attributes)
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT


            val title = dialog.findViewById<TextView>(R.id.title)
            val content = dialog.findViewById<TextView>(R.id.content)
            val btnClose = dialog.findViewById<AppCompatButton>(R.id.bt_close)
            title.text = titleText
            content.text = contentText

            if (!buttonTitle.isNullOrEmpty()) {
                btnClose.text = buttonTitle
            }
            btnClose.setOnClickListener { _: View? ->
                dialog.dismiss()
            }
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.window!!.attributes = layoutParams
        } catch (ex: Exception) {
            Sentry.captureException(ex)
        }
    }
}
