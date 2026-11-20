package com.akilimo.mobile.network

import okhttp3.Dns
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.net.InetAddress
import java.net.UnknownHostException

class FallbackDnsTest {

    private val address = InetAddress.getByAddress("host", byteArrayOf(1, 2, 3, 4))
    private val failing = Dns { throw UnknownHostException("EAI_NODATA") }
    private val working = Dns { listOf(address) }

    @Test
    fun `uses primary when it resolves`() {
        val dns = FallbackDns(working, listOf(failing))
        assertEquals(listOf(address), dns.lookup("host"))
    }

    @Test
    fun `falls back when primary fails`() {
        val dns = FallbackDns(failing, listOf(failing, working))
        assertEquals(listOf(address), dns.lookup("host"))
    }

    @Test
    fun `throws original failure when every resolver fails`() {
        val dns = FallbackDns(failing, listOf(failing))
        assertThrows(UnknownHostException::class.java) { dns.lookup("host") }
    }
}
