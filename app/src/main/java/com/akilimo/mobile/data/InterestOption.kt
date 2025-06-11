package com.akilimo.mobile.data

interface ValueOption {
    val displayLabel: String
    val value: String
}


fun List<ValueOption>.indexOfValue(value: String?): Int =
    indexOfFirst { it.value == value }.takeIf { it >= 0 } ?: -1

data class InterestOption(
    override val displayLabel: String, override val value: String
) : ValueOption

data class RiskOption(
    override val displayLabel: String, override val value: String, val riskAtt: Int
) : ValueOption

data class CountryOption(
    override val displayLabel: String, override val value: String, val currencyCode: String
) : ValueOption