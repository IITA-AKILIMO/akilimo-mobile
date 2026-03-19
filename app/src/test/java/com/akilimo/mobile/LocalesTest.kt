package com.akilimo.mobile

import org.junit.Assert.assertEquals
import org.junit.Test

class LocalesTest {

    @Test
    fun `normalize returns english tag for blank input`() {
        assertEquals("en-US", Locales.normalize(""))
        assertEquals("en-US", Locales.normalize("   "))
    }

    @Test
    fun `normalize maps short code en to en-US`() {
        assertEquals("en-US", Locales.normalize("en"))
    }

    @Test
    fun `normalize maps short code sw to sw-TZ`() {
        assertEquals("sw-TZ", Locales.normalize("sw"))
    }

    @Test
    fun `normalize maps short code rw to rw-RW`() {
        assertEquals("rw-RW", Locales.normalize("rw"))
    }

    @Test
    fun `normalize is case-insensitive for full BCP-47 tag`() {
        assertEquals("en-US", Locales.normalize("EN-US"))
        assertEquals("sw-TZ", Locales.normalize("SW-TZ"))
    }

    @Test
    fun `normalize returns english for unrecognised code`() {
        assertEquals("en-US", Locales.normalize("fr"))
        assertEquals("en-US", Locales.normalize("xyz"))
    }

    @Test
    fun `normalize passes through valid full BCP-47 tags`() {
        assertEquals("en-US", Locales.normalize("en-US"))
        assertEquals("sw-TZ", Locales.normalize("sw-TZ"))
        assertEquals("rw-RW", Locales.normalize("rw-RW"))
    }
}
