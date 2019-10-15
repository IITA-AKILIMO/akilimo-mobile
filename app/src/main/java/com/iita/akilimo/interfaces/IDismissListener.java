package com.iita.akilimo.interfaces;

import com.iita.akilimo.models.Fertilizer;

public interface IDismissListener {
    void onDismiss(boolean priceSpecified, Fertilizer fertilizer, boolean removeSelected);
}
