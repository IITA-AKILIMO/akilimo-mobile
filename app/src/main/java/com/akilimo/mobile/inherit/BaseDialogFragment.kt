package com.akilimo.mobile.inherit

import androidx.fragment.app.DialogFragment
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.dao.AppDatabase.Companion.getDatabase
import com.akilimo.mobile.utils.MathHelper
import com.akilimo.mobile.utils.SessionManager

abstract class BaseDialogFragment : DialogFragment() {
    protected var LOG_TAG: String = BaseDialogFragment::class.java.simpleName

    protected val mathHelper: MathHelper by lazy { MathHelper() }

    protected val sessionManager: SessionManager by lazy { SessionManager(requireContext()) }
    protected val database: AppDatabase by lazy { getDatabase(requireContext()) }

    protected var currencySymbol: String = "USD"
}
