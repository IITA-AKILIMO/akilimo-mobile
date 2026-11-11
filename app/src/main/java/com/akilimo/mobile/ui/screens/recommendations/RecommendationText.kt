package com.akilimo.mobile.ui.screens.recommendations

// The API sometimes joins sentences without a space ("profitable.Plant"); a lowercase letter or
// bracket before the stop avoids touching abbreviations such as "U.S.A".
private val MISSING_SPACE = Regex("(?<=[a-z)][.!?])(?=[A-Z])")
private val SENTENCE_END = Regex("(?<=[.!?])\\s+(?=[A-Z0-9(])")

/**
 * Turns the advice text returned by the API into short paragraphs (one per sentence) so long
 * advice reads as a list of steps instead of one dense block.
 */
internal fun splitRecommendationParagraphs(text: String): List<String> =
    text.lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .flatMap { line -> MISSING_SPACE.replace(line, " ").split(SENTENCE_END) }
        .map { it.trim() }
        .filter { it.isNotEmpty() }
