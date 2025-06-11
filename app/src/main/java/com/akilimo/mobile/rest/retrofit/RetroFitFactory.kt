package com.akilimo.mobile.rest.retrofit

import android.content.Context
import android.os.Build
import com.akilimo.mobile.BuildConfig
import com.akilimo.mobile.R
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object RetroFitFactory {

    fun create(context: Context, baseUrl: String, timeoutSeconds: Long = 30): Retrofit {
        val client = getCompatibleOkHttpClient(context, timeoutSeconds)

        val objectMapper = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(objectMapper))
            .build()
    }

    private fun getCompatibleOkHttpClient(context: Context, timeoutSeconds: Long): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) { // Android < 7.1.1
            try {
                val sslContextAndTrustManager = provideLegacySSLContext(context)
                builder.sslSocketFactory(
                    sslContextAndTrustManager.first,
                    sslContextAndTrustManager.second
                )
            } catch (e: Exception) {
                e.printStackTrace()
                // fallback to default if cert loading fails
            }
        }

        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(logging)
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
            .first()

        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, arrayOf(trustManager), null)
        }

        return sslContext.socketFactory to trustManager
    }
}
