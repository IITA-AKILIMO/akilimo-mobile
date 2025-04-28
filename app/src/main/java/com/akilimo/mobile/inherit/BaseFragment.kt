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
import com.akilimo.mobile.R
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.dao.AppDatabase.Companion.getDatabase
import com.akilimo.mobile.entities.UserLocation
import com.akilimo.mobile.utils.MathHelper
import com.akilimo.mobile.utils.SessionManager
import dev.b3nedikt.reword.Reword.reword
import io.sentry.Sentry

abstract class BaseFragment : Fragment() {
    protected var LOG_TAG: String = BaseFragment::class.java.simpleName

    protected var currency: String? = null
    protected var currencySymbol: String? = null
    protected var countryCode: String? = null
    protected var countryName: String? = null

    private var appVersion: String? = null

    protected val sessionManager: SessionManager by lazy { SessionManager(requireContext()) }
    protected val mathHelper: MathHelper by lazy { MathHelper() }
    protected val database: AppDatabase by lazy { getDatabase(requireContext()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
        appVersion = sessionManager.getAppVersion()
    }

    abstract fun refreshData()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = loadFragmentLayout(inflater, container, savedInstanceState)
        reword(view)
        return view
    }

    protected abstract fun loadFragmentLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View


    protected fun loadLocationInfo(userLocation: UserLocation?): StringBuilder {
        val stBuilder = StringBuilder()
        if (userLocation != null) {
            val latitude = userLocation.latitude.toString()
            val longitude = userLocation.longitude.toString()
            stBuilder.append("Lat:")
            stBuilder.append(latitude)
            stBuilder.append(" ")
            stBuilder.append("Lon:")
            stBuilder.append(longitude)
        }

        return stBuilder
    }


    protected fun showCustomWarningDialog(
        titleText: String?,
        contentText: String?,
        buttonTitle: String? = null
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
            btnClose.setOnClickListener { view: View? ->
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
