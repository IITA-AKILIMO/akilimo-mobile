@file:Suppress("SpellCheckingInspection")

package com.akilimo.mobile.network

import android.content.Context
import android.os.Build
import com.akilimo.mobile.BuildConfig
import com.akilimo.mobile.R
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.sentry.Sentry
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object ApiClient {

    fun buildRetrofit(
        context: Context,
        baseUrl: String,
        timeoutSeconds: Long
    ): Retrofit {
        val moshi = Moshi.Builder()
            .add(LocalDateAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()

        val client = getCompatibleOkHttpClient(context, timeoutSeconds)
            .dns(Dns.SYSTEM)
            .apply {
                if (BuildConfig.DEBUG) {
                    val logging = HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                        redactHeader("Authorization")
                    }
                    addInterceptor(logging)
                }
            }
            .addInterceptor(SafeNetworkInterceptor())
            .addInterceptor(RetryInterceptor())
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
        timeoutSeconds: Long = 60
    ): T {
        return buildRetrofit(context, baseUrl, timeoutSeconds).create(T::class.java)
    }

    private fun getCompatibleOkHttpClient(
        context: Context,
        timeoutSeconds: Long
    ): OkHttpClient.Builder {
        val builder = OkHttpClient.Builder()
            .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)
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

        return builder
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
