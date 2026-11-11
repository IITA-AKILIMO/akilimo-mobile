package com.akilimo.mobile.repos

import android.content.Context
import com.akilimo.mobile.config.AppConfig
import com.akilimo.mobile.enums.EnumCoverageStatus
import com.akilimo.mobile.enums.EnumServiceType
import com.akilimo.mobile.network.AkilimoApi
import com.akilimo.mobile.network.ApiClient
import com.akilimo.mobile.utils.CoverageArea
import io.sentry.Sentry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection

/**
 * The supported area (coverage) per country, from `GET /v1/coverage/{country}`.
 *
 * The GeoJSON is kept in `files/coverage/{CC}.geojson` with its ETag, so the map overlay and the
 * location check keep working offline. Each country is revalidated with the server once per app
 * process (a 304 costs no download); when the server cannot be reached the cached copy is used.
 */
class CoverageRepo(private val context: Context) {

    data class Coverage(val countryCode: String, val geoJson: String, val area: CoverageArea)

    private val mutex = Mutex()
    private val memory = mutableMapOf<String, Coverage>()

    private val cacheDir: File get() = File(context.filesDir, "coverage")

    /** The supported area of [countryCode], or null when none is known (see [EnumCoverageStatus.UNKNOWN]). */
    suspend fun get(countryCode: String): Coverage? {
        val code = countryCode.trim().uppercase()
        if (code.length != 2) return null

        return mutex.withLock {
            memory[code]?.let { return@withLock it }
            // Nothing known yet (offline with no cached copy, or no area on the server):
            // not remembered, so the next call tries again
            withContext(Dispatchers.IO) { load(code) }?.also { memory[code] = it }
        }
    }

    /** Is the point inside the supported area of [countryCode]? */
    suspend fun check(countryCode: String, lat: Double, lon: Double): EnumCoverageStatus {
        val coverage = get(countryCode) ?: return EnumCoverageStatus.UNKNOWN
        return if (coverage.area.contains(lat, lon)) EnumCoverageStatus.INSIDE else EnumCoverageStatus.OUTSIDE
    }

    private suspend fun load(code: String): Coverage? {
        val dataFile = File(cacheDir, "$code.geojson")
        val etagFile = File(cacheDir, "$code.etag")
        val cached = readCached(code, dataFile)

        return try {
            val base = AppConfig.resolveBaseUrlFor(context, EnumServiceType.AKILIMO)
            val api = ApiClient.createService<AkilimoApi>(context, base, timeoutSeconds = 30)
            val etag = if (cached != null) etagFile.takeIf { it.exists() }?.readText() else null
            val response = api.getCoverage(code, etag)

            when {
                response.code() == HttpURLConnection.HTTP_NOT_MODIFIED -> cached
                response.code() == HttpURLConnection.HTTP_NOT_FOUND -> {
                    // The API has no area for this country (any more): drop the stale copy
                    dataFile.delete()
                    etagFile.delete()
                    null
                }
                response.isSuccessful -> {
                    val text = response.body()?.string().orEmpty()
                    val area = CoverageArea.parse(text) ?: return cached
                    cacheDir.mkdirs()
                    dataFile.writeText(text)
                    response.headers()["ETag"]?.let { etagFile.writeText(it) } ?: etagFile.delete()
                    Coverage(code, text, area)
                }
                else -> cached
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Offline is normal in the field; only report real bugs
            if (e !is IOException) Sentry.captureException(e)
            cached
        }
    }

    private fun readCached(code: String, file: File): Coverage? = try {
        if (!file.exists()) {
            null
        } else {
            val text = file.readText()
            CoverageArea.parse(text)?.let { Coverage(code, text, it) }
        }
    } catch (e: IOException) {
        Sentry.captureException(e)
        null
    }
}
