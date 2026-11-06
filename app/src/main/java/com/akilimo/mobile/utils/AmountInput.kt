package com.akilimo.mobile.utils

import java.math.BigDecimal

/**
 * Parses money / quantity amounts typed by farmers, tolerating the separators people
 * actually use rather than only Kotlin's `toDouble()` format.
 *
 * Accepted:
 * - Plain numbers: `1500`, `1500.5`, `0.5`
 * - Comma decimal: `1,5`, `12,50`
 * - Thousands separators: `12,000`, `1,234,567`, `1.234.567`, `1 500`
 * - Both separators (the last one is the decimal point): `1,234.5`, `1.234,5`
 *
 * Ambiguity rule: a single comma followed by exactly three digits (`1,500`) is read as a
 * thousands separator, because prices are typed that way far more often than three-decimal
 * fractions. A single dot is always a decimal point, so values pre-filled with
 * [format] round-trip unchanged.
 *
 * Negative numbers, letters and malformed grouping (`1,2,3`) are rejected (null).
 */
object AmountInput {

    private val IGNORED = setOf(' ', ' ', '\'')

    fun parse(raw: String): Double? {
        val s = raw.filterNot { it.isWhitespace() || it in IGNORED }
        if (s.isEmpty() || s.none { it.isDigit() }) return null
        if (!s.all { it.isDigit() || it == '.' || it == ',' }) return null

        val lastComma = s.lastIndexOf(',')
        val lastDot = s.lastIndexOf('.')

        val normalized = when {
            lastComma >= 0 && lastDot >= 0 -> {
                val decimal = if (lastComma > lastDot) ',' else '.'
                val group = if (decimal == ',') '.' else ','
                if (s.count { it == decimal } != 1) return null
                val intPart = s.substringBefore(decimal)
                val fraction = s.substringAfter(decimal)
                if (!isGrouped(intPart, group)) return null
                intPart.replace(group.toString(), "") + "." + fraction
            }

            lastComma >= 0 -> normalizeSingleSeparator(s, ',') ?: return null
            lastDot >= 0 -> normalizeSingleSeparator(s, '.') ?: return null
            else -> s
        }

        return normalized.toDoubleOrNull()?.takeIf { it.isFinite() }
    }

    /**
     * True when the user typed something that is not an acceptable amount. Blank input is
     * not invalid (callers decide whether a field is required).
     */
    fun isInvalid(raw: String, requirePositive: Boolean): Boolean {
        if (raw.isBlank()) return false
        val value = parse(raw) ?: return true
        return requirePositive && value <= 0.0
    }

    /** Format a stored amount for pre-filling a field: `1500.0` -> `1500`, never `1.5E7`. */
    fun format(value: Double): String =
        BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()

    private fun normalizeSingleSeparator(s: String, sep: Char): String? {
        val count = s.count { it == sep }
        if (count > 1) {
            return if (isGrouped(s, sep)) s.replace(sep.toString(), "") else null
        }
        val intPart = s.substringBefore(sep)
        val fraction = s.substringAfter(sep)
        val looksLikeThousands = sep == ',' && fraction.length == 3 && intPart.length in 1..3
        return if (looksLikeThousands) intPart + fraction else "${intPart.ifEmpty { "0" }}.$fraction"
    }

    private fun isGrouped(intPart: String, sep: Char): Boolean {
        if (sep !in intPart) return intPart.isNotEmpty()
        val escaped = Regex.escape(sep.toString())
        return Regex("^\\d{1,3}($escaped\\d{3})+$").matches(intPart)
    }
}
