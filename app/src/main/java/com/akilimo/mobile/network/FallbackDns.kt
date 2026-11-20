package com.akilimo.mobile.network

import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import timber.log.Timber
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

/**
 * Resolves with the system resolver first and, when that fails (the intermittent
 * `EAI_NODATA` / `UnknownHostException` seen on flaky mobile networks), retries
 * through each fallback resolver in turn.
 */
class FallbackDns(
    private val primary: Dns = Dns.SYSTEM,
    private val fallbacks: List<Dns> = defaultFallbacks,
) : Dns {

    override fun lookup(hostname: String): List<InetAddress> {
        val failure = try {
            return primary.lookup(hostname)
        } catch (e: UnknownHostException) {
            e
        }

        for (fallback in fallbacks) {
            try {
                val addresses = fallback.lookup(hostname)
                if (addresses.isNotEmpty()) {
                    Timber.tag(TAG).w("System DNS failed for %s, resolved via fallback", hostname)
                    return addresses
                }
            } catch (e: Exception) {
                failure.addSuppressed(e)
            }
        }
        throw failure
    }

    companion object {
        private const val TAG = "FallbackDns"

        // DoH servers are reached by IP (bootstrap), so the fallback needs no DNS itself.
        private val defaultFallbacks: List<Dns> by lazy {
            val bootstrapClient = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build()

            fun doh(url: String, vararg ips: String) = DnsOverHttps.Builder()
                .client(bootstrapClient)
                .url(url.toHttpUrl())
                .bootstrapDnsHosts(ips.map { InetAddress.getByName(it) })
                .includeIPv6(false)
                .build()

            listOf(
                doh("https://cloudflare-dns.com/dns-query", "1.1.1.1", "1.0.0.1"),
                doh("https://dns.google/dns-query", "8.8.8.8", "8.8.4.4"),
                doh("https://dns.quad9.net/dns-query", "9.9.9.9", "149.112.112.112"),
                doh("https://doh.opendns.com/dns-query", "208.67.222.222", "208.67.220.220"),
            )
        }
    }
}
