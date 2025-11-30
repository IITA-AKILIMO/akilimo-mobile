package com.akilimo.mobile.enums

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class EnumUseCase : Parcelable {
    FR,
    SP,
    PP,
    CIS,
    CIM,
    NA;

    companion object {
        fun fromCode(code: String?): EnumUseCase {
            if (code.isNullOrBlank()) return NA
            return EnumUseCase.entries.firstOrNull { it.name.equals(code, ignoreCase = true) }
                ?: NA
        }
    }
}
