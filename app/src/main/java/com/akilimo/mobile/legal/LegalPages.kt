package com.akilimo.mobile.legal

import android.content.Context
import java.io.File

/**
 * The privacy policy and terms of use are published on akilimo.org. The app keeps a copy of each in its own
 * storage ([LegalPagesWorker] refreshes it) so they open offline; until the first download succeeds, or if the
 * copy is missing, the web address is used.
 */
enum class LegalPage(val fileName: String, val webUrl: String) {
    PRIVACY("privacy_policy.html", "https://akilimo.org/akilimo-privacy-policy.html"),
    TERMS("terms.html", "https://akilimo.org/akilimo-terms.html"),
}

object LegalPages {
    private const val DIR = "legal"

    fun file(context: Context, page: LegalPage): File = File(File(context.filesDir, DIR), page.fileName)

    /** The saved copy as a `file://` address when there is one, otherwise the published web address. */
    fun urlFor(context: Context, page: LegalPage): String {
        val saved = file(context, page)
        return if (saved.isFile && saved.length() > 0) saved.toURI().toString() else page.webUrl
    }
}
