package com.akilimo.mobile.utils.enums

import com.google.android.material.appbar.AppBarLayout

enum class EnumScrollFlags {
    NO_SCROLL {
        override fun scrollFlags(): Int {
            return AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
        }
    },
    SCROLL {
        override fun scrollFlags(): Int {
            return AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
        }
    };

    abstract fun scrollFlags(): Int
}
