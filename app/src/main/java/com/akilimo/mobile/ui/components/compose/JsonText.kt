package com.akilimo.mobile.ui.components.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import okio.Buffer

/** The kinds of JSON text that get their own colour in the technical details. */
enum class JsonTokenType { KEY, STRING, NUMBER, LITERAL }

/** One coloured piece of a pretty-printed JSON text: characters [start] until [end] (exclusive). */
data class JsonToken(val start: Int, val end: Int, val type: JsonTokenType)

private const val JSON_INDENT = "  "
private const val MAX_CAPTURE_GROUPS = 1

// A string (a key when a colon follows), a number, or true / false / null
private val TOKEN = Regex("\"(?:\\\\.|[^\"\\\\])*\"(\\s*:)?|-?\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?|\\btrue\\b|\\bfalse\\b|\\bnull\\b")

/**
 * The JSON object or array in [raw], indented two spaces per level with the keys in their original order,
 * or null when [raw] is not a JSON object or array (for example an HTML error page from a proxy).
 */
fun prettyJson(raw: String): String? {
    val text = raw.trim()
    if (!text.startsWith("{") && !text.startsWith("[")) return null
    return try {
        val reader = JsonReader.of(Buffer().writeUtf8(text))
        val out = Buffer()
        JsonWriter.of(out).use { writer ->
            writer.indent = JSON_INDENT
            writer.serializeNulls = true
            copyValue(reader, writer)
        }
        out.readUtf8()
    } catch (_: Exception) {
        null
    }
}

/** Copies the value at [reader] to [writer] token by token, so numbers keep their text (400 stays 400, never 400.0). */
private fun copyValue(reader: JsonReader, writer: JsonWriter) {
    when (reader.peek()) {
        JsonReader.Token.BEGIN_OBJECT -> {
            reader.beginObject()
            writer.beginObject()
            while (reader.hasNext()) {
                writer.name(reader.nextName())
                copyValue(reader, writer)
            }
            reader.endObject()
            writer.endObject()
        }

        JsonReader.Token.BEGIN_ARRAY -> {
            reader.beginArray()
            writer.beginArray()
            while (reader.hasNext()) copyValue(reader, writer)
            reader.endArray()
            writer.endArray()
        }

        JsonReader.Token.STRING -> writer.value(reader.nextString())
        JsonReader.Token.NUMBER -> reader.nextSource().use { source -> writer.valueSink().use { sink -> source.readAll(sink) } }
        JsonReader.Token.BOOLEAN -> writer.value(reader.nextBoolean())
        JsonReader.Token.NULL -> {
            reader.nextNull<Any>()
            writer.nullValue()
        }

        else -> throw IllegalStateException("Unexpected JSON token")
    }
}

/** The coloured pieces of a pretty-printed JSON [text], in order. The colon after a key is left uncoloured. */
fun jsonTokens(text: String): List<JsonToken> = TOKEN.findAll(text).map { match ->
    val value = match.value
    when {
        value.startsWith("\"") && match.groupValues.size > MAX_CAPTURE_GROUPS && match.groupValues[1].isNotEmpty() ->
            JsonToken(match.range.first, match.range.first + value.length - match.groupValues[1].length, JsonTokenType.KEY)

        value.startsWith("\"") -> JsonToken(match.range.first, match.range.last + 1, JsonTokenType.STRING)
        value == "true" || value == "false" || value == "null" ->
            JsonToken(match.range.first, match.range.last + 1, JsonTokenType.LITERAL)

        else -> JsonToken(match.range.first, match.range.last + 1, JsonTokenType.NUMBER)
    }
}.toList()

/** [pretty] JSON as text coloured from the theme: keys, strings, numbers and literals each get their own tone. */
@Composable
fun rememberJsonText(pretty: String): AnnotatedString {
    val key = MaterialTheme.colorScheme.primary
    val string = MaterialTheme.colorScheme.tertiary
    val number = MaterialTheme.colorScheme.secondary
    val literal = MaterialTheme.colorScheme.outline
    return remember(pretty, key, string, number, literal) {
        buildAnnotatedString {
            append(pretty)
            jsonTokens(pretty).forEach { token ->
                val color = when (token.type) {
                    JsonTokenType.KEY -> key
                    JsonTokenType.STRING -> string
                    JsonTokenType.NUMBER -> number
                    JsonTokenType.LITERAL -> literal
                }
                addStyle(SpanStyle(color = color), token.start, token.end)
            }
        }
    }
}
