package com.akilimo.mobile.ui.components.compose

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonTextTest {

    private val payload = """{"error":"Bad Request","message":"No data for lat=-9.287","status":400,"ok":false,"extra":null}"""

    @Test
    fun `json is indented two spaces per level with keys in their original order`() {
        val pretty = prettyJson(payload)

        assertEquals(
            """
            {
              "error": "Bad Request",
              "message": "No data for lat=-9.287",
              "status": 400,
              "ok": false,
              "extra": null
            }
            """.trimIndent(),
            pretty,
        )
    }

    @Test
    fun `nested objects and arrays are indented`() {
        val pretty = prettyJson("""{"errors":{"map_lat":["outside","range"]}}""")

        assertTrue(pretty!!.contains("\n    \"map_lat\": [\n      \"outside\","))
    }

    @Test
    fun `text that is not a json object or array has no json view`() {
        assertNull(prettyJson("<html>Bad gateway</html>"))
        assertNull(prettyJson("{broken"))
        assertNull(prettyJson("42"))
        assertNull(prettyJson(""))
    }

    @Test
    fun `tokens mark keys strings numbers and literals but not the colon`() {
        val text = prettyJson(payload)!!
        val tokens = jsonTokens(text)
        fun word(t: JsonToken) = text.substring(t.start, t.end)

        assertEquals("\"error\"", word(tokens.first { it.type == JsonTokenType.KEY }))
        assertEquals(listOf("\"error\"", "\"message\"", "\"status\"", "\"ok\"", "\"extra\""),
            tokens.filter { it.type == JsonTokenType.KEY }.map(::word))
        assertEquals(listOf("\"Bad Request\"", "\"No data for lat=-9.287\""),
            tokens.filter { it.type == JsonTokenType.STRING }.map(::word))
        assertEquals(listOf("400"), tokens.filter { it.type == JsonTokenType.NUMBER }.map(::word))
        assertEquals(listOf("false", "null"), tokens.filter { it.type == JsonTokenType.LITERAL }.map(::word))
    }

    @Test
    fun `numbers inside strings are not coloured as numbers`() {
        val text = prettyJson("""{"message":"value 12.5 here"}""")!!

        assertTrue(jsonTokens(text).none { it.type == JsonTokenType.NUMBER })
    }
}
