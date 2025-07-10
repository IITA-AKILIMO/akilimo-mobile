package com.akilimo.mobile.inherit

import android.app.Dialog
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.akilimo.mobile.R
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.entities.UserLocation
import com.akilimo.mobile.utils.MathHelper
import com.akilimo.mobile.utils.PreferenceManager
import com.stepstone.stepper.Step
import com.stepstone.stepper.VerificationError
import io.sentry.Sentry
import timber.log.Timber

/**
 * Base fragment implementing common functionality for stepper fragments
 */
@Deprecated("Remove and replace")
abstract class BaseStepFragment : Fragment(), Step {

    protected var errorMessage: String = ""
    open var dataIsValid = false

    // Lazy initialization for dependencies
    protected val database: AppDatabase by lazy { AppDatabase.getInstance(requireContext().applicationContext) }
    protected val preferenceManager: PreferenceManager by lazy { PreferenceManager(requireContext().applicationContext) }
    protected val mathHelper: MathHelper by lazy { MathHelper() }

    protected var verificationError: VerificationError? = null

    /**
     * Shows a custom warning dialog with customizable title, content and button text
     *
     * @param titleText Title of the dialog
     * @param contentText Content of the dialog (defaults to title text if null)
     * @param buttonTitle Custom text for the close button (optional)
     */
    protected fun showCustomWarningDialog(
        titleText: String, contentText: String? = titleText, buttonTitle: String? = null
    ) {
        try {
            Dialog(requireContext()).apply {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setContentView(R.layout.dialog_warning)
                setCancelable(false)
                setCanceledOnTouchOutside(false)

                // Set layout parameters
                window?.apply {
                    val params = attributes
                    params.width = ViewGroup.LayoutParams.WRAP_CONTENT
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    attributes = params
                }

                // Set dialog content
                findViewById<TextView>(R.id.title).text = titleText
                findViewById<TextView>(R.id.content).text = contentText

                // Configure close button
                findViewById<AppCompatButton>(R.id.bt_close)?.apply {
                    if (!buttonTitle.isNullOrBlank()) {
                        text = buttonTitle
                    }
                    setOnClickListener { dismiss() }
                }
                show()
            }
        } catch (ex: Exception) {
            Timber.e(ex, "Error showing warning dialog")
            Sentry.captureException(ex)
        }
    }

    /**
     * Creates a formatted string with location information
     *
     * @param userLocation Location data to format
     * @return Formatted location string
     */
    protected fun formatLocationInfo(userLocation: UserLocation?): String {
        if (userLocation == null) {
            return ""
        }
        val lat = mathHelper.removeLeadingZero(userLocation.latitude, "#.####")
        val lon = mathHelper.removeLeadingZero(userLocation.longitude, "#.####")
        val place = userLocation.locationCountryName

        return "${place}\n${lat},${lon}"
    }

    override fun onError(error: VerificationError) {
        // Base implementation - override in subclasses if needed
        Timber.e("Verification error: ${error.errorMessage}")
    }

    override fun onSelected() {
        Timber.d("Fragment selected")
    }
}