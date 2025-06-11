package com.akilimo.mobile.utils.converters

import androidx.room.TypeConverter
import com.akilimo.mobile.utils.enums.EnumAreaUnit

class Converters {
    @TypeConverter
    fun toAreaUnit(value: String) = enumValueOf<EnumAreaUnit>(value)

    @TypeConverter
    fun fromareaUnit(value: EnumAreaUnit) = value.name
}
