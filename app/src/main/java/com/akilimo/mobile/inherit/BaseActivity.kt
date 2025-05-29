package com.akilimo.mobile.inherit

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import com.akilimo.mobile.R
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.dao.AppDatabase.Companion.getDatabase
import com.akilimo.mobile.utils.LanguageManager
import com.akilimo.mobile.utils.MathHelper
import com.akilimo.mobile.utils.SessionManager
import com.akilimo.mobile.utils.enums.EnumCountry
import com.akilimo.mobile.views.activities.DstRecommendationActivity
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import io.sentry.Sentry

@SuppressLint("LogNotTimber")
abstract class BaseActivity : AppCompatActivity() {
    protected val LOG_TAG: String = this::class.java.simpleName

    protected val sessionManager: SessionManager by lazy { SessionManager(this@BaseActivity) }
    protected val database: AppDatabase by lazy { getDatabase(this@BaseActivity) }
    protected val mathHelper: MathHelper by lazy { MathHelper() }

    protected var countryCode: String = EnumCountry.Nigeria.countryCode()

    protected var currencyName: String = EnumCountry.Nigeria.currencyCode()
    protected var currencyCode: String = EnumCountry.Nigeria.currencyCode()
    protected var currencySymbol: String = EnumCountry.Nigeria.currencyCode()
    protected var baseCurrency: String = "USD"

    protected var areaUnit: String = "acre"
    protected var areaUnitText: String = "acre"
    protected var fieldSize: Double = 0.0
    protected var fieldSizeAcre: Double = 2.471

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        closeActivity(true)
    }

    override fun attachBaseContext(newBase: Context) {
        val language = LanguageManager.getLanguage(newBase)
        val context = LanguageManager.setLocale(newBase, language)
        super.attachBaseContext(context)
    }

    @Deprecated(
        message = "Remove completely and use setupToolbar(toolbar, titleResId) instead.",
        replaceWith = ReplaceWith("setupToolbar(binding.toolbarLayout.toolbar, R.string.your_title)"),
        level = DeprecationLevel.WARNING
    )
    protected abstract fun initToolbar()

    fun setupToolbar(
        toolbar: Toolbar,
        @StringRes titleResId: Int,
        showBackButton: Boolean = true,
        onBackClick: (() -> Unit)? = null
    ) {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = getString(titleResId)
            setDisplayHomeAsUpEnabled(showBackButton)
            setDisplayShowHomeEnabled(showBackButton)
        }

        if (showBackButton) {
            toolbar.setNavigationIcon(R.drawable.ic_left_arrow)
            toolbar.setNavigationOnClickListener {
                onBackClick?.invoke() ?: onBackPressedDispatcher.onBackPressed()
            }
        }
    }


    @Deprecated("Deprecated remove it completely")
    protected abstract fun initComponent()

    protected abstract fun validate(backPressed: Boolean)

    protected fun closeActivity(backPressed: Boolean) {
        if (!backPressed) {
            finish()
        }
        Animatoo.animateSlideLeft(this@BaseActivity)
    }

    protected fun openActivity(intent: Intent?) {
        intent?.let {
            startActivity(it)
            Animatoo.animateSlideRight(this@BaseActivity)
        }
    }


    protected fun showCustomNotificationDialog(
        titleText: String? = getString(R.string.title_realistic_price),
        contentText: String? = getString(R.string.lbl_realistic_price),
        buttonTitle: String? = null
    ) {

        val notificationCount = sessionManager.getNotificationCount()
        if (notificationCount <= 0) {
            return
        }
        sessionManager.updateNotificationCount(notificationCount)

        val dialog = Dialog(this)
        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE) // before
            setContentView(R.layout.dialog_notification)
            setCancelable(true)
        }

        val lp = WindowManager.LayoutParams()
        lp.apply {
            copyFrom(dialog.window!!.attributes)
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }


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

        dialog.show()
        dialog.window!!.attributes = lp
    }

    /**
     * @param titleText   title of the warning
     * @param contentText stepTitle of the warning
     */
    protected fun showCustomWarningDialog(
        titleText: String?,
        contentText: String?,
        buttonTitle: String? = null
    ) {
        try {
            val dialog = Dialog(this@BaseActivity)

            dialog.apply {
                requestWindowFeature(Window.FEATURE_NO_TITLE) // before
                setContentView(R.layout.dialog_warning)
                setCancelable(true)
            }

            val lp = WindowManager.LayoutParams()
            lp.apply {
                copyFrom(dialog.window!!.attributes)
                width = WindowManager.LayoutParams.WRAP_CONTENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }


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

            dialog.show()
            dialog.window!!.attributes = lp
        } catch (ex: Exception) {
            Sentry.captureException(ex)
        }
    }


    protected fun checkAppPermissions(rationale: String?) {
        val permissions = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.VIBRATE
        )

        val options = Permissions.Options()
            .setRationaleDialogTitle("Info")
            .setSettingsDialogTitle("Warning")

        Permissions.check(
            this,
            permissions,
            rationale,
            options,
            object : PermissionHandler() {
                override fun onGranted() {
                    // do your task.
                }

                override fun onDenied(context: Context, deniedPermissions: ArrayList<String>) {
                    super.onDenied(context, deniedPermissions)
                }
            })
    }

    protected fun processRecommendations(activity: Activity) {
        val intent = Intent(activity, DstRecommendationActivity::class.java)
        activity.startActivity(intent)
    }
}
