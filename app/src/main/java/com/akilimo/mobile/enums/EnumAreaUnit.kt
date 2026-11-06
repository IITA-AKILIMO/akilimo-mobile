package com.akilimo.mobile.enums

import android.content.Context
import com.akilimo.mobile.R

enum class EnumAreaUnit(private val stringRes: Int) : ILabelProvider {
    ACRE(R.string.lbl_acre),
    HA(R.string.lbl_ha),
    M2(R.string.lbl_m2),
    ARE(R.string.lbl_are);

    override fun label(context: Context): String = stringRes.let { context.getString(it) }

    companion object {
        /**
         * Parse a unit string from the API (e.g. investment amounts' `area_unit`).
         * Returns null when the value isn't recognised.
         */
        fun fromApiValue(raw: String?): EnumAreaUnit? =
            when (raw?.trim()?.lowercase()) {
                "acre", "acres", "ac" -> ACRE
                "ha", "hectare", "hectares" -> HA
                "m2", "m²", "sqm", "square meter", "square meters", "square metre", "square metres" -> M2
                "are", "ares", "a" -> ARE
                else -> entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) }
            }
    }
}
