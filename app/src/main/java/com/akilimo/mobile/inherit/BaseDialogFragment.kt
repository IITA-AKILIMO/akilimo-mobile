package com.akilimo.mobile.inherit

import androidx.fragment.app.DialogFragment
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.utils.MathHelper
import com.akilimo.mobile.utils.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

@Deprecated(
    "Use BaseBottomSheetDialogFragment instead",
    replaceWith = ReplaceWith("BaseBottomSheetDialogFragment")
)
abstract class BaseDialogFragment : DialogFragment() {
    protected val mathHelper: MathHelper by lazy { MathHelper() }
    protected val sessionManager: PreferenceManager by lazy { PreferenceManager(requireContext().applicationContext) }
    protected val database: AppDatabase by lazy { AppDatabase.getInstance(requireContext().applicationContext) }

    protected var currencySymbol: String = "USD"
}

abstract class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {
    protected val mathHelper: MathHelper by lazy { MathHelper() }
    protected val sessionManager: PreferenceManager by lazy { PreferenceManager(requireContext().applicationContext) }
    protected val database: AppDatabase by lazy { AppDatabase.getInstance(requireContext().applicationContext) }
    protected var currencySymbol: String = "USD"
}