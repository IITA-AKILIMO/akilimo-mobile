package com.akilimo.mobile.data

data class InterestOption(val label: String, val value: String)

fun List<InterestOption>.indexOfValue(value: String?): Int =
    indexOfFirst { it.value == value }.takeIf { it >= 0 } ?: 0
