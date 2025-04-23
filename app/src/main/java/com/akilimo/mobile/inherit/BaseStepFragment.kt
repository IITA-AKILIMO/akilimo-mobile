package com.akilimo.mobile.inherit

import android.app.Dialog
import android.content.Context
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
import com.akilimo.mobile.entities.LocationInfo
import com.akilimo.mobile.utils.MathHelper
import com.akilimo.mobile.utils.SessionManager
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.stepstone.stepper.Step
import com.stepstone.stepper.VerificationError
import dev.b3nedikt.app_locale.SharedPrefsAppLocaleRepository
import dev.b3nedikt.reword.Reword.reword
import io.sentry.Sentry
import java.util.Locale

abstract class BaseStepFragment protected constructor() : Fragment(), Step {
    protected var LOG_TAG: String = BaseStepFragment::class.java.simpleName

    @JvmField
    protected var currency: String? = null

    @JvmField
    protected var countryCode: String = ""

    @JvmField
    protected var countryName: String? = null

    @JvmField
    protected var errorMessage: String = ""

    @JvmField
    protected var database: AppDatabase? = null

    @JvmField
    protected var verificationError: VerificationError? = null

    protected var queue: RequestQueue? = null

    @JvmField
    protected var sessionManager: SessionManager? = null

    @JvmField
    protected var mathHelper: MathHelper? = null

    @JvmField
    @Deprecated("")
    protected var dataIsValid: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)

        val context = requireContext()
        sessionManager = SessionManager(context)
        queue = Volley.newRequestQueue(context.applicationContext)
        database = getDatabase(context)
        mathHelper = MathHelper()
    }

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
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View

    protected fun showCustomWarningDialog(
        titleText: String?,
        contentText: String? = titleText,
        buttonTitle: String? = null
    ) {
        val context = requireContext()
        try {
            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
            dialog.setContentView(R.layout.dialog_warning)
            dialog.setCancelable(true)

            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog.window!!.attributes)
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT


            val title = dialog.findViewById<TextView>(R.id.title)
            val content = dialog.findViewById<TextView>(R.id.content)
            val btnClose = dialog.findViewById<AppCompatButton>(R.id.bt_close)
            title.text = titleText
            content.text = contentText

            if (buttonTitle != null && btnClose != null && buttonTitle.isNotEmpty() && buttonTitle.length > 1) {
                btnClose.text = buttonTitle
            }
            btnClose!!.setOnClickListener { dialog.dismiss() }
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.window!!.attributes = layoutParams
        } catch (ex: Exception) {
            Sentry.captureException(ex)
        }
    }

    protected fun loadLocationInfo(locationInfo: LocationInfo?): StringBuilder {
        val stBuilder = StringBuilder()
        if (locationInfo != null) {
            val lat = mathHelper!!.removeLeadingZero(locationInfo.latitude, "#.####")
            val lon = mathHelper!!.removeLeadingZero(locationInfo.longitude, "#.####")

            val place = locationInfo.locationCountryName
            stBuilder.append(String.format("%s %n%s,%s", place, lat, lon))
        }

        return stBuilder
    }


    protected val currentLocale: Locale?
        get() {
            val prefs = SharedPrefsAppLocaleRepository(requireContext())
            var desiredLocale = prefs.desiredLocale
            if (desiredLocale != null) {
            }
            return desiredLocale
        }

    override fun onError(error: VerificationError) {
        //not implemented
    }
}
