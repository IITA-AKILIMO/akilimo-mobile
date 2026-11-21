package com.akilimo.mobile.ui.components.compose

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.R
import com.akilimo.mobile.network.dto.ReportKind
import com.akilimo.mobile.ui.theme.AkilimoSpacing

@StringRes
private fun ReportKind.labelRes(): Int = when (this) {
    ReportKind.FERTILIZER -> R.string.lbl_fertilizer_rec
    ReportKind.INTERCROP -> R.string.lbl_intercrop_rec
    ReportKind.PLANTING -> R.string.lbl_planting_practices_rec
    ReportKind.SCHEDULED -> R.string.lbl_scheduled_planting_rec
}

/**
 * The PDF reports of one recommendation: a button per kind, disabled when that PDF does not exist. The links are
 * signed, so they open in the browser or PDF viewer without any sign-in.
 */
@Composable
fun ReportDownloads(
    links: Map<ReportKind, String>,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
) {
    val context = LocalContext.current
    val openFailed = stringResource(R.string.msg_report_open_failed)

    FormCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AkilimoSpacing.sm)) {
            Text(text = stringResource(R.string.lbl_report_title), style = MaterialTheme.typography.titleMedium)
            if (loading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
        }
        Column(verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.sm)) {
            ReportKind.entries.forEach { kind ->
                val url = links[kind]
                OutlinedButton(
                    onClick = {
                        if (url != null) {
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            } catch (_: ActivityNotFoundException) {
                                Toast.makeText(context, openFailed, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    enabled = url != null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(text = stringResource(kind.labelRes()), modifier = Modifier.padding(start = AkilimoSpacing.sm))
                }
            }
        }
        if (links.isEmpty() && !loading) {
            Text(
                text = stringResource(R.string.msg_report_unavailable),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
