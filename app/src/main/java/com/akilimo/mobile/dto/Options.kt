package com.akilimo.mobile.dto

import com.akilimo.mobile.enums.EnumAdvice
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumAreaUnit
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.enums.EnumInvestmentPref
import com.akilimo.mobile.enums.EnumMaizePerformance
import com.akilimo.mobile.enums.EnumOperationMethod
import com.akilimo.mobile.enums.EnumOperationType
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.enums.EnumUnitOfSale
import com.akilimo.mobile.enums.EnumWeedControlMethod

/**
 * Base interface for options with a value
 */
interface BaseValueOption<T> {
    val valueOption: T
}

/**
 * Interface for options with both value and display label
 */
interface ValueOption<T> : BaseValueOption<T> {
    val displayLabel: String
}

/**
 * Find the index of an option by its value, returns -1 if not found
 */
fun <T> List<ValueOption<T>>.indexOfValue(value: T?): Int =
    indexOfFirst { it.valueOption == value }.takeIf { it >= 0 } ?: -1

/**
 * Find an option by its value, returns null if not found
 */
fun <T> List<ValueOption<T>>.findByValue(value: T?): ValueOption<T>? =
    firstOrNull { it.valueOption == value }

data class WrappedValueOption<T>(
    override val valueOption: T
) : BaseValueOption<T>


// User Interest Options
data class InterestOption(
    override val displayLabel: String,
    override val valueOption: String
) : ValueOption<String>

// Location Options
data class CountryOption(
    override val displayLabel: String,
    override val valueOption: EnumCountry,
    val currencyCode: String
) : ValueOption<EnumCountry>

// Language Options
data class LanguageOption(
    override val displayLabel: String,
    override val valueOption: String,
    val languageCode: String
) : ValueOption<String>

// Measurement Options
data class AreaUnitOption(
    override val displayLabel: String,
    override val valueOption: EnumAreaUnit
) : ValueOption<EnumAreaUnit>

data class FieldSizeOption(
    override val displayLabel: String,
    override val valueOption: Double
) : ValueOption<Double>

// Planting Options
data class PlantingFlexOption(
    override val displayLabel: String,
    override val valueOption: Long
) : ValueOption<Long>

// Operation Options
data class OperationTypeOption(
    override val displayLabel: String,
    override val valueOption: EnumOperationType
) : ValueOption<EnumOperationType>

data class OperationMethodOption(
    override val displayLabel: String,
    override val valueOption: EnumOperationMethod
) : ValueOption<EnumOperationMethod>

// Investment Options
data class InvestmentPrefOption(
    override val displayLabel: String,
    override val valueOption: EnumInvestmentPref
) : ValueOption<EnumInvestmentPref>

// Advice Options
data class AdviceOption(
    override val valueOption: EnumAdvice
) : BaseValueOption<EnumAdvice>

data class UseCaseOption(
    override val valueOption: EnumAdviceTask,
    var stepStatus: EnumStepStatus = EnumStepStatus.NOT_STARTED
) : BaseValueOption<EnumAdviceTask>

data class UnitOfSaleOption(
    override val valueOption: EnumUnitOfSale
) : BaseValueOption<EnumUnitOfSale>

data class WeedControlOption(
    override val valueOption: EnumWeedControlMethod,
) : BaseValueOption<EnumWeedControlMethod>

data class MaizePerfOption(
    override val valueOption: EnumMaizePerformance,
    val isSelected: Boolean = false
) : BaseValueOption<EnumMaizePerformance>