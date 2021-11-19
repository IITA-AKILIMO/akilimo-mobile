package com.akilimo.mobile.utils.converters

import androidx.room.TypeConverter
import com.akilimo.mobile.utils.enums.EnumAreaUnits

class Converters {
    @TypeConverter
    fun toAreaUnit(value: String) = enumValueOf<EnumAreaUnits>(value)

    @TypeConverter
    fun fromareaUnit(value: EnumAreaUnits) = value.name
}
