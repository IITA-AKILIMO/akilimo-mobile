package com.iita.akilimo.utils.enums;

import com.google.android.material.appbar.AppBarLayout.LayoutParams;

public enum EnumScrollFlags {
    NO_SCROLL {
        @Override
        public int scrollFlags() {
            return LayoutParams.SCROLL_FLAG_ENTER_ALWAYS;
        }
    },
    SCROLL {
        @Override
        public int scrollFlags() {
            return LayoutParams.SCROLL_FLAG_SCROLL | LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED;
        }
    },
    ;

    public abstract int scrollFlags();
}
