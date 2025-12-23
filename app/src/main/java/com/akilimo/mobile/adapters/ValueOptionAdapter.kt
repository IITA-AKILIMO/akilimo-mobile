package com.akilimo.mobile.adapters

import android.content.Context
import com.akilimo.mobile.dto.BaseValueOption
import com.akilimo.mobile.dto.ValueOption

class ValueOptionAdapter<T>(
    context: Context,
    options: List<ValueOption<T>>
) : BaseSpinnerAdapter<T, ValueOption<T>>(
    context = context,
    options = options,
    getDisplayText = { item -> item.displayLabel }
)

class BaseValueOptionAdapter<T>(
    context: Context,
    options: List<BaseValueOption<T>>,
    getDisplayText: (T) -> String
) : BaseSpinnerAdapter<T, BaseValueOption<T>>(
    context = context,
    options = options,
    getDisplayText = { item -> getDisplayText(item.valueOption) }
)

