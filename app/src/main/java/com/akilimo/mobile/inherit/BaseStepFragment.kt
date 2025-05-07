package com.akilimo.mobile.inherit

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.akilimo.mobile.R
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.entities.UserLocation
import com.akilimo.mobile.utils.MathHelper
import com.akilimo.mobile.utils.SessionManager
import com.stepstone.stepper.Step
import com.stepstone.stepper.VerificationError
import dev.b3nedikt.app_locale.SharedPrefsAppLocaleRepository
import dev.b3nedikt.reword.Reword.reword
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Locale

/**
 * Base fragment implementing common functionality for stepper fragments
 */
abstract class BaseStepFragment : Fragment(), Step {

    @Deprecated("Remove")
    protected var currency: String = ""

    @Deprecated("Remove")
    protected var countryCode: String = ""

    @Deprecated("Remove")
    protected var countryName: String = ""


    protected var errorMessage: String = ""
    protected var dataIsValid = false

    // Lazy initialization for dependencies
    protected val database: AppDatabase by lazy { AppDatabase.getDatabase(requireContext()) }
    protected val sessionManager: SessionManager by lazy { SessionManager(requireContext()) }
    protected val mathHelper: MathHelper by lazy { MathHelper() }

    protected var verificationError: VerificationError? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return loadFragmentLayout(inflater, container, savedInstanceState).also { view ->
            reword(view)
        }
    }

    /**
     * Load the specific layout for this fragment
     * @return The inflated view
     */
    protected abstract fun loadFragmentLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View

    /**
     * Shows a custom warning dialog with customizable title, content and button text
     *
     * @param titleText Title of the dialog
     * @param contentText Content of the dialog (defaults to title text if null)
     * @param buttonTitle Custom text for the close button (optional)
     */
    protected fun showCustomWarningDialog(
        titleText: String,
        contentText: String? = titleText,
        buttonTitle: String? = null
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

    /**
     * Gets the current locale from shared preferences
     *
     * @return Current locale or null if not set
     */
    protected fun getCurrentLocale(): Locale? {
        return SharedPrefsAppLocaleRepository(requireContext()).desiredLocale
    }

    /**
     * Helper function to perform database operations safely in a coroutine
     */
    protected fun performDatabaseOperation(
        operation: suspend () -> Unit,
        onError: (Exception) -> Unit = { Sentry.captureException(it) }
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    operation()
                }
            } catch (e: Exception) {
                Timber.e(e, "Database operation failed")
                onError(e)
            }
        }
    }

    override fun onError(error: VerificationError) {
        // Base implementation - override in subclasses if needed
        Timber.e("Verification error: ${error.errorMessage}")
    }
}