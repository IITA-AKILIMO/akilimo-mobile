@file:Suppress("SpellCheckingInspection")

package com.akilimo.mobile.network

import android.content.Context
import android.os.Build
import com.akilimo.mobile.BuildConfig
import com.akilimo.mobile.R
import com.squareup.moshi.Moshi
import io.sentry.Sentry
import io.sentry.okhttp.SentryOkHttpInterceptor
import okhttp3.Cache
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object ApiClient {

    private const val CONNECT_TIMEOUT_SECONDS = 15L

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            // Models use @JsonClass(generateAdapter = true); no reflective fallback is registered
            .add(LocalDateAdapter())
            .build()
    }

    private const val CACHE_SIZE_BYTES = 5L * 1024 * 1024

    // One Cache per directory: two Cache instances on the same folder corrupt each other
    private val caches = ConcurrentHashMap<String, Cache>()

    private fun cacheFor(context: Context, baseUrl: String): Cache {
        val host = baseUrl.toHttpUrl().host
        return caches.getOrPut(host) {
            Cache(File(context.applicationContext.cacheDir, "http_$host"), CACHE_SIZE_BYTES)
        }
    }

    @Volatile
    private var baseClient: OkHttpClient? = null

    /**
     * One shared client so every service reuses the same connection pool, dispatcher and DNS
     * resolver; per-service clients are derived from it with `newBuilder()`.
     */
    private fun sharedClient(context: Context): OkHttpClient =
        baseClient ?: synchronized(this) {
            baseClient ?: createBaseClient(context.applicationContext).also { baseClient = it }
        }

    /** Drops pooled connections; call when the network changes so no call reuses a dead socket. */
    fun resetConnections() {
        baseClient?.connectionPool?.evictAll()
    }

    fun buildRetrofit(
        context: Context,
        baseUrl: String,
        timeoutSeconds: Long,
        sendAkilimoApiKey: Boolean = true,
        bearerToken: (() -> String?)? = null,
        cacheMaxAgeSeconds: Long? = null,
    ): Retrofit {
        val client = sharedClient(context).newBuilder()
            // A short connect timeout fails fast on dead networks so retry/DNS fallback can act
            .connectTimeout(minOf(timeoutSeconds, CONNECT_TIMEOUT_SECONDS), TimeUnit.SECONDS)
            .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)
            // Hard cap across all retry attempts (RetryInterceptor makes up to 3)
            .callTimeout(timeoutSeconds * 3 + 10, TimeUnit.SECONDS)
            .apply {
                // Only AKILIMO's own backends may receive the AKILIMO API key;
                // third-party services (weather, geocoding) must opt out.
                if (sendAkilimoApiKey) {
                    addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .addHeader("X-Api-Key", BuildConfig.AKILIMO_API_KEY)
                            .build()
                        chain.proceed(request)
                    }
                }
            }
            .apply {
                // A signed-in farmer's token, read on every call so a sign-in or sign-out applies at once
                if (bearerToken != null) {
                    addInterceptor { chain ->
                        val token = bearerToken()
                        val request = if (token.isNullOrBlank()) {
                            chain.request()
                        } else {
                            chain.request().newBuilder().header("Authorization", "Bearer $token").build()
                        }
                        chain.proceed(request)
                    }
                }
            }
            .apply {
                // Cache GET responses for services whose servers send no usable Cache-Control
                if (cacheMaxAgeSeconds != null) {
                    cache(cacheFor(context, baseUrl))
                    addNetworkInterceptor { chain ->
                        val response = chain.proceed(chain.request())
                        if (chain.request().method == "GET") {
                            response.newBuilder()
                                .removeHeader("Pragma")
                                .header("Cache-Control", "public, max-age=$cacheMaxAgeSeconds")
                                .build()
                        } else {
                            response
                        }
                    }
                }
            }
            .addInterceptor(SafeNetworkInterceptor())
            .addInterceptor(RetryInterceptor())
            .apply {
                // Breadcrumbs for AKILIMO calls only: third-party calls carry API keys in the URL query
                if (sendAkilimoApiKey) addInterceptor(SentryOkHttpInterceptor())
            }
            .apply {
                // Added after RetryInterceptor so every attempt is logged, not just the first
                if (BuildConfig.DEBUG) {
                    val logging = HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                        redactHeader("Authorization")
                        redactHeader("X-Api-Key")
                    }
                    addInterceptor(logging)
                }
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    inline fun <reified T> createService(
        context: Context,
        baseUrl: String,
        timeoutSeconds: Long = 60,
        sendAkilimoApiKey: Boolean = true,
        noinline bearerToken: (() -> String?)? = null,
        cacheMaxAgeSeconds: Long? = null,
    ): T {
        return buildRetrofit(
            context, baseUrl, timeoutSeconds, sendAkilimoApiKey, bearerToken, cacheMaxAgeSeconds
        ).create(T::class.java)
    }

    private fun createBaseClient(context: Context): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .dns(FallbackDns())
            // Transparently retries once on a stale pooled connection or alternate route
            .retryOnConnectionFailure(true)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            try {
                val (sslSocketFactory, trustManager) = provideLegacySSLContext(context)
                builder.sslSocketFactory(sslSocketFactory, trustManager)
            } catch (e: Exception) {
                Sentry.captureException(e)
                Timber.tag("ApiClient").e(e, "SSL setup failed")
            }
        }

        return builder.build()
    }

    private fun provideLegacySSLContext(context: Context): Pair<SSLSocketFactory, X509TrustManager> {
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val caInput: InputStream = context.resources.openRawResource(R.raw.isrg_root_x1)
        val ca: Certificate = caInput.use { certificateFactory.generateCertificate(it) }

        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(null, null)
            setCertificateEntry("isrg", ca)
        }

        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
            init(keyStore)
        }

        val trustManager = tmf.trustManagers
            .filterIsInstance<X509TrustManager>()
            .firstOrNull() ?: throw IllegalStateException("No X509TrustManager found")

        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, arrayOf(trustManager), null)
        }

        return sslContext.socketFactory to trustManager
    }
}
