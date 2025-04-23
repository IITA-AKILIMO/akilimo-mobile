package com.akilimo.mobile.inherit

import android.content.Context
import androidx.fragment.app.DialogFragment
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.dao.AppDatabase.Companion.getDatabase
import com.akilimo.mobile.utils.MathHelper
import com.akilimo.mobile.utils.SessionManager

abstract class BaseDialogFragment : DialogFragment() {
    protected var LOG_TAG: String = BaseDialogFragment::class.java.simpleName


    @JvmField
    protected var mathHelper: MathHelper? = null
    @JvmField
    protected var database: AppDatabase? = null
    protected var sessionManager: SessionManager? = null
    @JvmField
    protected var currencySymbol: String? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mathHelper = MathHelper()
        database = getDatabase(context)
    }
}
