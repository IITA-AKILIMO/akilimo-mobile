package com.akilimo.mobile.utils

import androidx.room.TypeConverter
import com.akilimo.mobile.dto.OperationEntry
import com.akilimo.mobile.enums.EnumAdvice
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumAreaUnit
import com.akilimo.mobile.enums.EnumCassavaProduceType
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.enums.EnumUnitOfSale
import com.akilimo.mobile.enums.EnumWeedControlMethod
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate

/**
 * Type converters to allow Room to reference complex data types.
 */
object Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString() // ISO-8601 format (yyyy-MM-dd)
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }
}

object EnumTillageConverters {

    private val gson = Gson()

    @TypeConverter
    fun fromOperationEntryList(value: List<OperationEntry>?): String {
        return gson.toJson(value ?: emptyList<OperationEntry>())
    }

    @TypeConverter
    fun toOperationEntryList(value: String): List<OperationEntry> {
        return gson.fromJson(value, object : TypeToken<List<OperationEntry>>() {}.type)
    }

}

class EnumAreaUnitConverter {
    @TypeConverter
    fun fromEnum(value: EnumAreaUnit?): String? = EnumTypeConverter.fromEnum(value)

    @TypeConverter
    fun toEnum(value: String?): EnumAreaUnit? =
        EnumTypeConverter.toEnum(value, EnumAreaUnit::class.java)
}

class EnumWeedControlConverter {
    @TypeConverter
    fun fromEnum(value: EnumWeedControlMethod?): String? = EnumTypeConverter.fromEnum(value)

    @TypeConverter
    fun toEnum(value: String?): EnumWeedControlMethod? =
        EnumTypeConverter.toEnum(value, EnumWeedControlMethod::class.java)
}

class EnumProduceTypeConverter {
    @TypeConverter
    fun fromEnum(value: EnumCassavaProduceType?): String? = EnumTypeConverter.fromEnum(value)

    @TypeConverter
    fun toEnum(value: String?): EnumCassavaProduceType? =
        EnumTypeConverter.toEnum(value, EnumCassavaProduceType::class.java)
}

class EnumUnitOfSaleConverter {
    @TypeConverter
    fun fromEnum(value: EnumUnitOfSale?): String? = EnumTypeConverter.fromEnum(value)

    @TypeConverter
    fun toEnum(value: String?): EnumUnitOfSale? =
        EnumTypeConverter.toEnum(value, EnumUnitOfSale::class.java)
}

class EnumAdviseConverter {
    @TypeConverter
    fun fromEnum(value: EnumAdvice?): String? = EnumTypeConverter.fromEnum(value)

    @TypeConverter
    fun toEnum(value: String?): EnumAdvice? =
        EnumTypeConverter.toEnum(value, EnumAdvice::class.java)
}

class EnumAdviseTaskConverter {
    @TypeConverter
    fun fromEnum(value: EnumAdviceTask?): String? = EnumTypeConverter.fromEnum(value)

    @TypeConverter
    fun toEnum(value: String?): EnumAdviceTask? =
        EnumTypeConverter.toEnum(value, EnumAdviceTask::class.java)
}

class EnumStepStatusConverter {
    @TypeConverter
    fun fromEnum(value: EnumStepStatus?): String? = EnumTypeConverter.fromEnum(value)

    @TypeConverter
    fun toEnum(value: String?): EnumStepStatus? =
        EnumTypeConverter.toEnum(value, EnumStepStatus::class.java)
}


object EnumTypeConverter {
    @TypeConverter
    fun <T : Enum<T>> fromEnum(value: T?): String? = value?.name

    @TypeConverter
    fun <T : Enum<T>> toEnum(value: String?, enumClass: Class<T>): T? {
        if (value == null) return null
        return enumClass.enumConstants?.firstOrNull { it.name == value }
    }
}